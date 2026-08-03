package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.PurchaseItemResponse;
import com.bookworm.backend.dto.response.PurchaseTransactionResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;

/**
 * Pure PDFBox rendering, no service/repository dependencies at all - the
 * PurchaseTransactionResponse and customer name/email are supplied by the
 * caller instead of being fetched here. Deliberately a leaf component so
 * both InvoiceServiceImpl (download-by-id endpoint) and PurchaseServiceImpl
 * (checkout receipt email) can depend on it without creating a
 * PurchaseServiceImpl -> InvoiceService -> PurchaseService circular bean
 * dependency - InvoiceServiceImpl needs PurchaseService to look up a
 * transaction by id, but PurchaseServiceImpl already has the transaction
 * in hand at checkout time and just needs the rendering logic, not the
 * lookup. Extracted verbatim from the original InvoiceServiceImpl.
 */
@Component
public class InvoicePdfRenderer {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 18;

    public byte[] render(PurchaseTransactionResponse tx, String customerName, String customerEmail) {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType1Font bold = PDType1Font.HELVETICA_BOLD;
            PDType1Font regular = PDType1Font.HELVETICA;

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float y = PDRectangle.A4.getHeight() - MARGIN;

                y = writeLine(content, bold, 18, MARGIN, y, "Bookworm.com");
                y = writeLine(content, regular, 10, MARGIN, y, "Digital Library & Marketplace");
                y -= LINE_HEIGHT;

                y = writeLine(content, bold, 13, MARGIN, y, "Invoice #" + tx.getPurchaseTransactionId());
                y = writeLine(content, regular, 10, MARGIN, y,
                        "Date: " + tx.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")));
                y = writeLine(content, regular, 10, MARGIN, y, "Status: " + tx.getStatus());
                y -= LINE_HEIGHT;

                y = writeLine(content, bold, 11, MARGIN, y, "Billed to");
                y = writeLine(content, regular, 10, MARGIN, y, sanitize(customerName));
                y = writeLine(content, regular, 10, MARGIN, y, sanitize(customerEmail));
                y -= LINE_HEIGHT;

                y = writeLine(content, bold, 11, MARGIN, y, "Items");
                float col2 = PDRectangle.A4.getWidth() - MARGIN - 80;
                for (PurchaseItemResponse item : tx.getItems()) {
                    content.beginText();
                    content.setFont(regular, 10);
                    content.newLineAtOffset(MARGIN, y);
                    content.showText(sanitize(truncate(item.getProductTitle(), 60)));
                    content.endText();

                    content.beginText();
                    content.setFont(regular, 10);
                    content.newLineAtOffset(col2, y);
                    content.showText("Rs. " + item.getUnitPrice());
                    content.endText();

                    y -= LINE_HEIGHT;
                }
                y -= LINE_HEIGHT / 2;

                y = writeLine(content, bold, 12, MARGIN, y, "Total: Rs. " + tx.getTotalAmount());
                if (tx.getLoyaltyPointsEarned() != null && tx.getLoyaltyPointsEarned() > 0) {
                    y = writeLine(content, regular, 10, MARGIN, y,
                            "Loyalty points earned: " + tx.getLoyaltyPointsEarned());
                }

                y -= LINE_HEIGHT * 2;
                writeLine(content, regular, 8, MARGIN, y, "Thank you for shopping with Bookworm.com.");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException("Failed to generate invoice PDF", ex);
        }
    }

    private float writeLine(PDPageContentStream content, PDType1Font font, float fontSize,
                             float x, float y, String text) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(text);
        content.endText();
        return y - LINE_HEIGHT;
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 1) + "..." : text;
    }

    // The standard Helvetica font (WinAnsiEncoding) only supports Latin-1 -
    // several of this catalog's real titles are in Devanagari script (Marathi/
    // Hindi/Konkani, per the bulk-imported "Prod Master Table.xlsx"), which
    // would otherwise throw IllegalArgumentException at render time. Falling
    // back to '?' per unsupported character keeps invoice generation from
    // crashing on real demo data; a proper fix would embed a Unicode font
    // (e.g. Noto Sans Devanagari) instead of substituting placeholders.
    private String sanitize(String text) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder(text.length());
        for (char c : text.toCharArray()) {
            sb.append(c <= 0xFF ? c : '?');
        }
        return sb.toString();
    }
}
