package com.bookworm.backend.mapper;

import com.bookworm.backend.dto.response.UserShelfResponse;
import com.bookworm.backend.entity.Product;
import com.bookworm.backend.entity.UserShelf;
import org.springframework.stereotype.Component;

@Component
public class UserShelfMapper {

    public UserShelfResponse toResponse(UserShelf entity) {
        Product product = entity.getProduct();
        return UserShelfResponse.builder()
                .userShelfId(entity.getUserShelfId())
                .productId(product.getProductId())
                .productTitle(product.getTitle())
                .coverImage(product.getCoverImage())
                .filePath(product.getFilePath())
                .fileType(product.getFileType())
                .source(entity.getSource())
                .expiresAt(entity.getExpiresAt())
                .acquiredAt(entity.getAcquiredAt())
                .build();
    }
}
