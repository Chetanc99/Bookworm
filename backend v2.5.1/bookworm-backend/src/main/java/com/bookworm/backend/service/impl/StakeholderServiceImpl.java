package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.request.StakeholderRequest;
import com.bookworm.backend.dto.response.StakeholderResponse;
import com.bookworm.backend.entity.Stakeholder;
import com.bookworm.backend.exception.ResourceNotFoundException;
import com.bookworm.backend.mapper.StakeholderMapper;
import com.bookworm.backend.repository.StakeholderRepository;
import com.bookworm.backend.service.StakeholderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StakeholderServiceImpl implements StakeholderService {

    private final StakeholderRepository stakeholderRepository;
    private final StakeholderMapper stakeholderMapper;

    @Override
    public List<StakeholderResponse> getAllActive() {
        return stakeholderRepository.findByIsActiveTrue().stream().map(stakeholderMapper::toResponse).toList();
    }

    @Override
    public List<StakeholderResponse> getAll() {
        return stakeholderRepository.findAll().stream().map(stakeholderMapper::toResponse).toList();
    }

    @Override
    public StakeholderResponse getById(Long stakeholderId) {
        return stakeholderMapper.toResponse(findEntityOrThrow(stakeholderId));
    }

    @Override
    @Transactional
    public StakeholderResponse create(StakeholderRequest request) {
        Stakeholder stakeholder = Stakeholder.builder()
                .name(request.getName())
                .type(request.getType())
                .description(request.getDescription())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        return stakeholderMapper.toResponse(stakeholderRepository.save(stakeholder));
    }

    @Override
    @Transactional
    public StakeholderResponse update(Long stakeholderId, StakeholderRequest request) {
        Stakeholder stakeholder = findEntityOrThrow(stakeholderId);
        stakeholder.setName(request.getName());
        stakeholder.setType(request.getType());
        stakeholder.setDescription(request.getDescription());
        if (request.getIsActive() != null) stakeholder.setIsActive(request.getIsActive());
        return stakeholderMapper.toResponse(stakeholderRepository.save(stakeholder));
    }

    @Override
    @Transactional
    public void delete(Long stakeholderId) {
        Stakeholder stakeholder = findEntityOrThrow(stakeholderId);
        stakeholder.setIsActive(false);
        stakeholderRepository.save(stakeholder);
    }

    private Stakeholder findEntityOrThrow(Long stakeholderId) {
        return stakeholderRepository.findById(stakeholderId)
                .orElseThrow(() -> new ResourceNotFoundException("Stakeholder", "stakeholder_id", stakeholderId));
    }
}
