package com.bookworm.backend.service.impl;

import com.bookworm.backend.dto.response.UserShelfResponse;
import com.bookworm.backend.mapper.UserShelfMapper;
import com.bookworm.backend.repository.UserShelfRepository;
import com.bookworm.backend.service.UserShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserShelfServiceImpl implements UserShelfService {

    private final UserShelfRepository userShelfRepository;
    private final UserShelfMapper mapper;

    @Override
    public List<UserShelfResponse> getShelf(Long userId) {
        return userShelfRepository.findByUserIdOrderByAcquiredAtDesc(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }
}
