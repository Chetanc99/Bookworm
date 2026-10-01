package com.bookworm.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class PurchaseItemResponse {
    private Long purchaseItemId;
    private Long productId;
    private String productTitle;
    private BigDecimal unitPrice;
    // The product's own base language name (e.g. "Marathi", "English") and,
    // when that base language isn't English, the English ProductTranslation
    // title if one exists - used by InvoicePdfRenderer to pick which title to
    // print (requirement #20: English products print as-is, non-English
    // products print the English title on the invoice). Both are additive
    // fields - existing clients that only read productTitle are unaffected.
    private String productLanguageName;
    private String englishTitle;
}
