package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.LibraryPackageRequest;
import com.bookworm.backend.dto.request.ProductRequest;
import com.bookworm.backend.dto.response.BulkImportResponse;
import com.bookworm.backend.dto.response.BulkImportRowResult;
import com.bookworm.backend.dto.response.ProductResponse;
import com.bookworm.backend.entity.Genre;
import com.bookworm.backend.entity.Language;
import com.bookworm.backend.entity.ProductCategory;
import com.bookworm.backend.entity.ProductSubcategory;
import com.bookworm.backend.repository.GenreRepository;
import com.bookworm.backend.repository.LanguageRepository;
import com.bookworm.backend.repository.ProductCategoryRepository;
import com.bookworm.backend.repository.ProductSubcategoryRepository;
import com.bookworm.backend.service.AuditService;
import com.bookworm.backend.service.LibraryPackageService;
import com.bookworm.backend.service.ProductBulkImportService;
import com.bookworm.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Column headers (row 1, any order, case-insensitive): Title, Category,
 * Subcategory, Genre (optional), Language, Price, Pages (optional),
 * Duration (optional), IsRentable (optional), RentRate (required if
 * IsRentable), MinRentDays (required if IsRentable), IsLibraryEligible
 * (optional), FileType (optional), CoverImage (optional path/URL),
 * FilePath (optional path/URL), ShortDescription (optional), Description
 * (optional).
 *
 * Category/Subcategory/Genre/Language are looked up by name (case-
 * insensitive) and created on the fly if missing - same lazy-create
 * convenience already used for the CUSTOMER role in AuthServiceImpl and the
 * bootstrap ADMIN role in AdminBootstrapConfig, so an admin filling out a
 * spreadsheet doesn't need to pre-create taxonomy rows first.
 *
 * Deliberately NOT @Transactional at the class/method level: each row goes
 * through ProductService.create (which manages its own transaction), so one
 * bad row fails and rolls back only itself - earlier/later rows in the same
 * upload still commit independently. See BulkImportResponse for per-row results.
 */
@Service
@RequiredArgsConstructor
public class ProductBulkImportServiceImpl implements ProductBulkImportService {

    private final ProductService productService;
    private final LibraryPackageService libraryPackageService;
    private final ProductCategoryRepository categoryRepository;
    private final ProductSubcategoryRepository subcategoryRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;
    private final AuditService auditService;

    // Recognized language names in "Type/Language/category"-style compound columns
    // (Magento-style export, see importLegacyRow). Anything unrecognized falls
    // back to the raw token as-is so it still gets created rather than dropped.
    private static final Map<String, String> LANGUAGE_ALIASES = Map.ofEntries(
            Map.entry("मराठी", "Marathi"),
            Map.entry("हिंदी", "Hindi"),
            Map.entry("हिन्दी", "Hindi"),
            Map.entry("कोकणी", "Konkani"),
            Map.entry("कोंकणी", "Konkani"),
            Map.entry("english", "English")
    );

    @Override
    public BulkImportResponse importProducts(MultipartFile file) {
        List<BulkImportRowResult> results = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                throw new IllegalArgumentException("Sheet has no header row");
            }
            Map<String, Integer> columnIndex = new HashMap<>();
            for (Cell cell : headerRow) {
                String header = formatter.formatCellValue(cell).trim().toLowerCase();
                if (StringUtils.hasText(header)) {
                    columnIndex.put(header, cell.getColumnIndex());
                }
            }

            // Two supported schemas, auto-detected from the header row:
            //  1. Our own generic "Title/Category/Subcategory/..." schema (original).
            //  2. The real Magento-style export the catalog data actually comes in
            //     ("prod name", "_attribute_set", "Type/Language/category", ...).
            boolean isLegacyExport = columnIndex.containsKey("prod name");

            for (int rowNum = sheet.getFirstRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isBlankRow(row, formatter)) continue;

                int displayRow = rowNum + 1; // 1-based, matches what a spreadsheet user sees
                String title = isLegacyExport
                        ? cellValue(row, columnIndex, formatter, "prod name")
                        : cellValue(row, columnIndex, formatter, "title");
                try {
                    if (isLegacyExport) {
                        results.add(importLegacyRow(row, columnIndex, formatter, displayRow));
                    } else {
                        ProductResponse created = importRow(row, columnIndex, formatter);
                        results.add(BulkImportRowResult.builder()
                                .rowNumber(displayRow)
                                .success(true)
                                .title(created.getTitle())
                                .productId(created.getProductId())
                                .build());
                    }
                } catch (Exception ex) {
                    results.add(BulkImportRowResult.builder()
                            .rowNumber(displayRow)
                            .success(false)
                            .title(title)
                            .errorMessage(ex.getMessage())
                            .build());
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to read uploaded workbook", ex);
        }

        long successCount = results.stream().filter(BulkImportRowResult::isSuccess).count();
        BulkImportResponse response = BulkImportResponse.builder()
                .totalRows(results.size())
                .successCount((int) successCount)
                .failureCount(results.size() - (int) successCount)
                .results(results)
                .build();

        auditService.log("BULK_IMPORT", "PRODUCT", null,
                response.getSuccessCount() + "/" + response.getTotalRows() + " rows imported successfully");

        return response;
    }

    /**
     * Parses one row of the real Prod Master Table.xlsx export. Columns:
     * prod name, _attribute_set, Type/Language/category, availability, Author,
     * description, short_description, is_package, price, special_price,
     * meta_description, meta_keyword, meta_title, Publisher,
     * product_name_in_english, product_type, status.
     *
     * Known, deliberate lossy mappings (no equivalent field on our backend
     * schema - flagged here rather than silently dropped):
     *  - Author / Publisher: appended into the product description text.
     *  - meta_description / meta_keyword / meta_title: dropped (SEO-only,
     *    nothing in ProductRequest/LibraryPackageRequest to hold them).
     *  - special_price vs price: our schema has one price field. We store
     *    special_price when present (that's the real selling price the
     *    storefront would show), else fall back to price.
     *  - Rows with is_package = Yes are NOT products at all - they're a
     *    library subscription tier (e.g. "Granthalay - 30 Days Package").
     *    These are routed to LibraryPackageService.create instead, with
     *    maxConcurrentBorrows/durationDays best-effort parsed out of the
     *    short_description HTML ("Books Available in Package : N",
     *    "Package Available for : N Days"), falling back to durationDays=30/
     *    maxConcurrentBorrows=5 if the text doesn't match.
     */
    private BulkImportRowResult importLegacyRow(Row row, Map<String, Integer> columnIndex,
                                                 DataFormatter formatter, int displayRow) {
        String prodName = requireText(row, columnIndex, formatter, "prod name");
        boolean isPackage = cellBoolean(row, columnIndex, formatter, "is_package");
        BigDecimal price = requireDecimal(row, columnIndex, formatter, "price");
        String rawShortDescription = stripHtml(cellValue(row, columnIndex, formatter, "short_description"));

        if (isPackage) {
            int maxConcurrentBorrows = extractNumber(rawShortDescription,
                    "books?\\s*available\\s*in\\s*package\\s*:?\\s*(\\d+)", 5);
            int durationDays = extractNumber(rawShortDescription,
                    "package\\s*available\\s*for\\s*:?\\s*(\\d+)", 30);

            String packageDescription = stripHtml(cellValue(row, columnIndex, formatter, "description"));

            LibraryPackageRequest request = new LibraryPackageRequest();
            request.setPackageName(prodName.length() > 100 ? prodName.substring(0, 100) : prodName);
            // LibraryPackage.description is capped at 500 chars (DB column length) -
            // same truncation already applied to Product.shortDescription below.
            request.setDescription(packageDescription != null && packageDescription.length() > 500
                    ? packageDescription.substring(0, 500) : packageDescription);
            request.setPrice(price);
            request.setDurationDays(durationDays);
            request.setMaxConcurrentBorrows(maxConcurrentBorrows);

            libraryPackageService.create(request);
            return BulkImportRowResult.builder()
                    .rowNumber(displayRow)
                    .success(true)
                    .title(prodName + " (library package)")
                    .build();
        }

        String attributeSet = cellValue(row, columnIndex, formatter, "_attribute_set"); // eBooks / Audiobooks / Videos
        String categoryName = StringUtils.hasText(attributeSet) ? attributeSet : "Uncategorized";

        // "Type/Language/category" e.g. "e-Book/मराठी/ललित" -> [type, language, genre]
        String compound = cellValue(row, columnIndex, formatter, "type/language/category");
        String languageToken = null;
        String genreToken = null;
        if (StringUtils.hasText(compound)) {
            String[] parts = compound.split("/");
            if (parts.length >= 2) languageToken = parts[1].trim();
            if (parts.length >= 3) genreToken = parts[2].trim();
        }
        if (!StringUtils.hasText(languageToken)) languageToken = "Unspecified";
        String languageName = LANGUAGE_ALIASES.getOrDefault(languageToken.toLowerCase(), languageToken);

        BigDecimal specialPrice = cellDecimalOrNull(row, columnIndex, formatter, "special_price");
        BigDecimal effectivePrice = specialPrice != null ? specialPrice : price;

        String author = cellValue(row, columnIndex, formatter, "author");
        String publisher = cellValue(row, columnIndex, formatter, "publisher");
        String description = stripHtml(cellValue(row, columnIndex, formatter, "description"));
        StringBuilder fullDescription = new StringBuilder();
        if (StringUtils.hasText(description)) fullDescription.append(description).append("\n\n");
        if (StringUtils.hasText(author)) fullDescription.append("Author: ").append(author).append("\n");
        if (StringUtils.hasText(publisher)) fullDescription.append("Publisher: ").append(publisher);

        ProductCategory category = categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .orElseGet(() -> categoryRepository.save(ProductCategory.builder().categoryName(categoryName).build()));

        // No true subcategory breakdown exists in this export; default to a
        // subcategory named after the category itself so the taxonomy still
        // resolves (documented assumption - can be re-split by hand later).
        ProductSubcategory subcategory = subcategoryRepository
                .findBySubcategoryNameIgnoreCaseAndCategory_CategoryId(categoryName, category.getCategoryId())
                .orElseGet(() -> subcategoryRepository.save(ProductSubcategory.builder()
                        .category(category)
                        .subcategoryName(categoryName)
                        .build()));

        Long genreId = null;
        if (StringUtils.hasText(genreToken)) {
            final String genreName = genreToken;
            Genre genre = genreRepository
                    .findByGenreNameIgnoreCaseAndSubcategory_SubcategoryId(genreName, subcategory.getSubcategoryId())
                    .orElseGet(() -> genreRepository.save(Genre.builder()
                            .subcategory(subcategory)
                            .genreName(genreName)
                            .build()));
            genreId = genre.getGenreId();
        }

        Language language = languageRepository.findByLanguageNameIgnoreCase(languageName)
                .orElseGet(() -> languageRepository.save(Language.builder().languageName(languageName).build()));

        String fileType = switch (categoryName.toLowerCase()) {
            case "ebooks", "e-books", "e-book" -> "PDF";
            case "audiobooks", "audio books" -> "MP3";
            case "videos" -> "MP4";
            default -> "PDF";
        };

        ProductRequest request = new ProductRequest();
        request.setSubcategoryId(subcategory.getSubcategoryId());
        request.setGenreId(genreId);
        request.setLanguageId(language.getLanguageId());
        request.setTitle(prodName.length() > 200 ? prodName.substring(0, 200) : prodName);
        request.setShortDescription(rawShortDescription != null && rawShortDescription.length() > 500
                ? rawShortDescription.substring(0, 500) : rawShortDescription);
        request.setDescription(fullDescription.length() > 0 ? fullDescription.toString() : null);
        request.setPrice(effectivePrice);
        request.setCoverImage(null);
        request.setFilePath(null);
        request.setFileType(fileType);
        request.setIsRentable(false);
        // This catalog is explicitly lending-library content ("Granthalay"),
        // so imported eBooks/Audiobooks/Videos default to library-eligible.
        request.setIsLibraryEligible(true);
        request.setIsAvailable(true);

        ProductResponse created = productService.create(request);
        return BulkImportRowResult.builder()
                .rowNumber(displayRow)
                .success(true)
                .title(created.getTitle())
                .productId(created.getProductId())
                .build();
    }

    private static String stripHtml(String value) {
        if (value == null) return null;
        String noTags = value.replaceAll("<[^>]+>", " ");
        String unescaped = noTags
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&ldquo;", "\"")
                .replace("&rdquo;", "\"")
                .replace("&lsquo;", "'")
                .replace("&rsquo;", "'");
        String collapsed = unescaped.replaceAll("\\s+", " ").trim();
        return StringUtils.hasText(collapsed) ? collapsed : null;
    }

    private static int extractNumber(String text, String pattern, int fallback) {
        if (text == null) return fallback;
        Matcher m = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
                // fall through to fallback
            }
        }
        return fallback;
    }

    private BigDecimal cellDecimalOrNull(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, String header) {
        String value = cellValue(row, columnIndex, formatter, header);
        if (!StringUtils.hasText(value)) return null;
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    // NOTE: no @Transactional here on purpose - a self-invoked protected method
    // wouldn't go through Spring's proxy anyway (classic self-invocation gap),
    // and it isn't needed: each repository .save() below runs in its own
    // Spring Data-managed transaction, and productService.create() (a
    // separate bean, called normally) manages its own transaction correctly.
    private ProductResponse importRow(Row row, Map<String, Integer> columnIndex, DataFormatter formatter) {
        String title = requireText(row, columnIndex, formatter, "title");
        String categoryName = requireText(row, columnIndex, formatter, "category");
        String subcategoryName = requireText(row, columnIndex, formatter, "subcategory");
        String genreName = cellValue(row, columnIndex, formatter, "genre");
        String languageName = requireText(row, columnIndex, formatter, "language");
        BigDecimal price = requireDecimal(row, columnIndex, formatter, "price");

        ProductCategory category = categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .orElseGet(() -> categoryRepository.save(ProductCategory.builder().categoryName(categoryName).build()));

        ProductSubcategory subcategory = subcategoryRepository
                .findBySubcategoryNameIgnoreCaseAndCategory_CategoryId(subcategoryName, category.getCategoryId())
                .orElseGet(() -> subcategoryRepository.save(ProductSubcategory.builder()
                        .category(category)
                        .subcategoryName(subcategoryName)
                        .build()));

        Long genreId = null;
        if (StringUtils.hasText(genreName)) {
            Genre genre = genreRepository
                    .findByGenreNameIgnoreCaseAndSubcategory_SubcategoryId(genreName, subcategory.getSubcategoryId())
                    .orElseGet(() -> genreRepository.save(Genre.builder()
                            .subcategory(subcategory)
                            .genreName(genreName)
                            .build()));
            genreId = genre.getGenreId();
        }

        Language language = languageRepository.findByLanguageNameIgnoreCase(languageName)
                .orElseGet(() -> languageRepository.save(Language.builder().languageName(languageName).build()));

        boolean isRentable = cellBoolean(row, columnIndex, formatter, "isrentable");
        boolean isLibraryEligible = cellBoolean(row, columnIndex, formatter, "islibraryeligible");

        ProductRequest request = new ProductRequest();
        request.setSubcategoryId(subcategory.getSubcategoryId());
        request.setGenreId(genreId);
        request.setLanguageId(language.getLanguageId());
        request.setTitle(title);
        request.setShortDescription(cellValue(row, columnIndex, formatter, "shortdescription"));
        request.setDescription(cellValue(row, columnIndex, formatter, "description"));
        request.setPrice(price);
        request.setPages(cellInteger(row, columnIndex, formatter, "pages"));
        request.setDuration(cellInteger(row, columnIndex, formatter, "duration"));
        request.setCoverImage(cellValue(row, columnIndex, formatter, "coverimage"));
        request.setFilePath(cellValue(row, columnIndex, formatter, "filepath"));
        request.setFileType(cellValue(row, columnIndex, formatter, "filetype"));
        request.setIsRentable(isRentable);
        request.setIsLibraryEligible(isLibraryEligible);
        if (isRentable) {
            request.setRentRate(requireDecimal(row, columnIndex, formatter, "rentrate"));
            request.setMinRentDays(requireInteger(row, columnIndex, formatter, "minrentdays"));
        }
        request.setIsAvailable(true);

        return productService.create(request);
    }

    private boolean isBlankRow(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (StringUtils.hasText(formatter.formatCellValue(cell))) return false;
        }
        return true;
    }

    private String cellValue(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, String header) {
        Integer idx = columnIndex.get(header);
        if (idx == null) return null;
        Cell cell = row.getCell(idx);
        if (cell == null) return null;
        String value = formatter.formatCellValue(cell).trim();
        return StringUtils.hasText(value) ? value : null;
    }

    private String requireText(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, String header) {
        String value = cellValue(row, columnIndex, formatter, header);
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException("Missing required column: " + header);
        }
        return value;
    }

    private BigDecimal requireDecimal(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, String header) {
        String value = requireText(row, columnIndex, formatter, header);
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid number for column '" + header + "': " + value);
        }
    }

    private Integer cellInteger(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, String header) {
        String value = cellValue(row, columnIndex, formatter, header);
        if (value == null) return null;
        try {
            return (int) Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid integer for column '" + header + "': " + value);
        }
    }

    private Integer requireInteger(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, String header) {
        Integer value = cellInteger(row, columnIndex, formatter, header);
        if (value == null) {
            throw new IllegalArgumentException("Missing required column: " + header);
        }
        return value;
    }

    private boolean cellBoolean(Row row, Map<String, Integer> columnIndex, DataFormatter formatter, String header) {
        String value = cellValue(row, columnIndex, formatter, header);
        if (value == null) return false;
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true") || normalized.equals("yes") || normalized.equals("1") || normalized.equals("y");
    }
}
