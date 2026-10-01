package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.PurchaseItemResponse;
import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

/**
 * Pure PDFBox rendering, no service/repository dependencies at all - the
 * PurchaseTransactionResponse and customer name/email are supplied by the
 * caller instead of being fetched here (see the class's original javadoc on
 * why this is a leaf component, preserved below).
 *
 * UNICODE / DEVANAGARI RENDERING
 * The previous version used PDFBox's built-in Helvetica (a Standard-14 font,
 * WinAnsiEncoding-only) and replaced every character outside Latin-1 with
 * '?' before rendering, since Helvetica has no Devanagari glyphs at all and
 * would otherwise throw at render time. That produced literal "????" for
 * every Marathi/Hindi/Konkani product title (this catalog's bulk-imported
 * "Prod Master Table.xlsx" data is largely in Devanagari script).
 *
 * The fix: embed a real Unicode TTF font (PDType0Font, which supports full
 * Unicode via a CID-keyed encoding, unlike the Standard-14 fonts) instead of
 * PDFBox's built-in fonts, and stop sanitizing away non-Latin-1 characters.
 * Font files are loaded once from the classpath (src/main/resources/fonts/)
 * - see FONT_REGULAR_RESOURCE/FONT_BOLD_RESOURCE below for the exact
 * filenames expected. "Noto Sans Devanagari" was chosen because Google's
 * Noto family bundles both Devanagari AND standard Latin glyphs in the same
 * font file, so one embedded font covers "Bookworm.com", "Rs.", English
 * titles, and Devanagari titles all in the same document with no font
 * switching mid-line.
 *
 * KNOWN LIMITATION (documented, not silently ignored): PDFBox draws glyphs
 * exactly in the code-point order given to it and does not perform complex
 * script shaping - it does not reorder matras, form conjunct consonants, or
 * apply the reordering rules Devanagari text needs for visually-correct
 * rendering the way a full text-shaping engine (e.g. HarfBuzz, or a browser's
 * text layout engine) would. In practice this means: character SET support
 * is now correct (no more "?" and no more crash), and simple Devanagari text
 * will look correct, but complex conjunct clusters may render as separate
 * glyphs in logical rather than final visual order. A fully shaped-correct
 * PDF would require an external shaping library PDFBox does not ship with -
 * out of scope for a PDFBox-only fix, called out explicitly rather than
 * silently claimed as "fixed" when it's "correct character set, best-effort
 * shaping."
 *
 * If the font files aren't present on the classpath (e.g. a fresh checkout
 * where an admin hasn't added them yet), rendering falls back to the
 * previous Helvetica-plus-'?'-substitution behavior rather than failing the
 * whole purchase/invoice flow - see loadFont()/sanitizeForFallback().
 */
@Component
@Slf4j
public class InvoicePdfRenderer {

    // Drop actual Noto Sans Devanagari .ttf files at these classpath locations
    // to enable proper Unicode/Devanagari rendering (e.g. download "Noto Sans
    // Devanagari" from Google Fonts: NotoSansDevanagari-Regular.ttf and
    // NotoSansDevanagari-Bold.ttf, renamed to the filenames below).
    private static final String FONT_REGULAR_RESOURCE = "fonts/NotoSansDevanagari-Regular.ttf";
    private static final String FONT_BOLD_RESOURCE = "fonts/NotoSansDevanagari-Bold.ttf";

    private static final float MARGIN = 48;
    private static final float LINE_HEIGHT = 16;

    // Bookworm brand color (amber-500, matching the frontend's Tailwind theme) -
    // used for the header band and section rules so the PDF visually matches
    // the storefront rather than looking like a generic monospace receipt.
    private static final PDColor BRAND_AMBER = new PDColor(new float[]{0.96f, 0.62f, 0.04f}, PDDeviceRGB.INSTANCE);
    private static final PDColor INK = new PDColor(new float[]{0.12f, 0.12f, 0.14f}, PDDeviceRGB.INSTANCE);
    private static final PDColor MUTED = new PDColor(new float[]{0.45f, 0.45f, 0.48f}, PDDeviceRGB.INSTANCE);
    private static final PDColor ROW_ALT = new PDColor(new float[]{0.96f, 0.96f, 0.97f}, PDDeviceRGB.INSTANCE);
    private static final PDColor RULE = new PDColor(new float[]{0.85f, 0.85f, 0.87f}, PDDeviceRGB.INSTANCE);

    public byte[] render(PurchaseTransactionResponse tx, String customerName, String customerEmail) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDFont bold = loadFont(document, FONT_BOLD_RESOURCE, PDType1Font.HELVETICA_BOLD);
            PDFont regular = loadFont(document, FONT_REGULAR_RESOURCE, PDType1Font.HELVETICA);
            boolean unicodeCapable = bold instanceof PDType0Font && regular instanceof PDType0Font;

            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float contentWidth = pageWidth - 2 * MARGIN;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                // --- Header band -------------------------------------------------
                float headerHeight = 64;
                content.setNonStrokingColor(BRAND_AMBER);
                content.addRect(0, pageHeight - headerHeight, pageWidth, headerHeight);
                content.fill();

                content.setNonStrokingColor(INK);
                writeText(content, bold, 20, MARGIN, pageHeight - 34, "Bookworm.com");
                writeText(content, regular, 10, MARGIN, pageHeight - 50, "Digital Library & Marketplace");

                String invoiceLabel = "INVOICE";
                writeTextRightAligned(content, bold, 16, pageWidth - MARGIN, pageHeight - 32, invoiceLabel);
                writeTextRightAligned(content, regular, 10, pageWidth - MARGIN, pageHeight - 48,
                        "#" + tx.getPurchaseTransactionId());

                float y = pageHeight - headerHeight - 34;

                // --- Meta row: date/status left, billed-to right ------------------
                content.setNonStrokingColor(MUTED);
                writeText(content, regular, 9, MARGIN, y, "DATE");
                content.setNonStrokingColor(INK);
                writeText(content, regular, 10, MARGIN, y - 13,
                        tx.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));

                content.setNonStrokingColor(MUTED);
                writeText(content, regular, 9, MARGIN, y - 32, "STATUS");
                content.setNonStrokingColor(INK);
                writeText(content, regular, 10, MARGIN, y - 45, String.valueOf(tx.getStatus()));

                float rightColX = pageWidth / 2f + 10;
                content.setNonStrokingColor(MUTED);
                writeText(content, regular, 9, rightColX, y, "BILLED TO");
                content.setNonStrokingColor(INK);
                writeText(content, regular, 10, rightColX, y - 13, safeText(customerName, unicodeCapable));
                writeText(content, regular, 10, rightColX, y - 26, safeText(customerEmail, unicodeCapable));

                y -= 62;
                drawRule(content, MARGIN, y, pageWidth - MARGIN);
                y -= 20;

                // --- Items table ---------------------------------------------------
                float col1X = MARGIN + 8;
                float col2X = pageWidth - MARGIN - 80;
                float tableTop = y;

                content.setNonStrokingColor(INK);
                writeText(content, bold, 10, col1X, y, "ITEM");
                writeTextRightAligned(content, bold, 10, pageWidth - MARGIN - 8, y, "PRICE");
                y -= 10;
                drawRule(content, MARGIN, y, pageWidth - MARGIN);
                y -= 16;

                boolean alt = false;
                for (PurchaseItemResponse item : tx.getItems()) {
                    if (alt) {
                        content.setNonStrokingColor(ROW_ALT);
                        content.addRect(MARGIN, y - 4, contentWidth, LINE_HEIGHT + 2);
                        content.fill();
                    }
                    content.setNonStrokingColor(INK);
                    writeText(content, regular, 10, col1X, y, safeText(truncate(invoiceTitleFor(item), 70), unicodeCapable));
                    writeTextRightAligned(content, regular, 10, pageWidth - MARGIN - 8, y, "Rs. " + item.getUnitPrice());
                    y -= LINE_HEIGHT;
                    alt = !alt;
                }

                y -= 6;
                drawRule(content, MARGIN, y, pageWidth - MARGIN);
                y -= 22;

                // --- Totals block, right-aligned ------------------------------------
                float totalsLabelX = pageWidth - MARGIN - 190;
                content.setNonStrokingColor(MUTED);
                writeText(content, regular, 10, totalsLabelX, y, "Total Amount");
                content.setNonStrokingColor(INK);
                writeTextRightAligned(content, bold, 14, pageWidth - MARGIN - 8, y, "Rs. " + tx.getTotalAmount());
                y -= 20;

                if (tx.getLoyaltyPointsEarned() != null && tx.getLoyaltyPointsEarned() > 0) {
                    content.setNonStrokingColor(MUTED);
                    writeText(content, regular, 9, totalsLabelX, y, "Loyalty points earned");
                    content.setNonStrokingColor(INK);
                    writeTextRightAligned(content, regular, 10, pageWidth - MARGIN - 8, y,
                            String.valueOf(tx.getLoyaltyPointsEarned()));
                    y -= LINE_HEIGHT;
                }

                // --- Footer -----------------------------------------------------------
                float footerY = MARGIN + 30;
                drawRule(content, MARGIN, footerY + 16, pageWidth - MARGIN);
                content.setNonStrokingColor(MUTED);
                writeText(content, regular, 8, MARGIN, footerY,
                        "Thank you for shopping with Bookworm.com. This is a system-generated invoice.");
                writeText(content, regular, 8, MARGIN, footerY - 12,
                        "For support, contact support@bookworm.com.");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to generate invoice PDF", ex);
        }
    }

    /**
     * Requirement #20 (invoice language rules): an English-language product
     * prints its own title as-is; a non-English product prints its English
     * ProductTranslation title instead, falling back to the original title
     * when no English translation has been added for it (see
     * PurchaseMapper.toItemResponse, which populates productLanguageName/
     * englishTitle on PurchaseItemResponse - this method just picks between
     * the two fields it already computed, no translation lookup happens here).
     */
    private String invoiceTitleFor(PurchaseItemResponse item) {
        if (item.getProductLanguageName() == null || "english".equalsIgnoreCase(item.getProductLanguageName())) {
            return item.getProductTitle();
        }
        return item.getEnglishTitle() != null ? item.getEnglishTitle() : item.getProductTitle();
    }

    private PDFont loadFont(PDDocument document, String resourcePath, PDFont fallback) {
        try (InputStream in = new ClassPathResource(resourcePath).getInputStream()) {
            return PDType0Font.load(document, in);
        } catch (IOException ex) {
            log.warn("Devanagari/Unicode invoice font not found at classpath:{} - falling back to {} "
                            + "(non-Latin-1 characters, e.g. Marathi/Hindi titles, will render as '?'). "
                            + "Add the font file to src/main/resources/{} to enable proper rendering.",
                    resourcePath, fallback.getName(), resourcePath);
            return fallback;
        }
    }

    /** With the Unicode font embedded, text passes through untouched; only the Helvetica fallback still needs '?' substitution. */
    private String safeText(String text, boolean unicodeCapable) {
        if (text == null) return "";
        return unicodeCapable ? text : sanitizeForFallback(text);
    }

    private String sanitizeForFallback(String text) {
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            sb.append(c <= 0xFF ? c : '?');
        }
        return sb.toString();
    }

    private void writeText(PDPageContentStream content, PDFont font, float fontSize,
                            float x, float y, String text) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text == null ? "" : text);
        content.endText();
    }

    private void writeTextRightAligned(PDPageContentStream content, PDFont font, float fontSize,
                                        float rightEdgeX, float y, String text) throws IOException {
        float width = font.getStringWidth(text == null ? "" : text) / 1000f * fontSize;
        writeText(content, font, fontSize, rightEdgeX - width, y, text);
    }

    private void drawRule(PDPageContentStream content, float x1, float y, float x2) throws IOException {
        content.setStrokingColor(RULE);
        content.setLineWidth(0.75f);
        content.moveTo(x1, y);
        content.lineTo(x2, y);
        content.stroke();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 1) + "..." : text;
    }
}
