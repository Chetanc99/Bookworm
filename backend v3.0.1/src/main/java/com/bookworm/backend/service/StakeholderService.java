package com.bookworm.backend.service;

import com.bookworm.backend.dto.request.StakeholderRequest;
import com.bookworm.backend.dto.response.StakeholderResponse;

import java.util.List;

public interface StakeholderService {
    List<StakeholderResponse> getAllActive();
    List<StakeholderResponse> getAll();
    StakeholderResponse getById(Long stakeholderId);
    StakeholderResponse create(StakeholderRequest request);
    StakeholderResponse update(Long stakeholderId, StakeholderRequest request);
    void delete(Long stakeholderId);
}
