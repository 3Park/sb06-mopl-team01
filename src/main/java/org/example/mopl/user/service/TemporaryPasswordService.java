package org.example.mopl.user.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.user.repository.TemporaryPasswordRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemporaryPasswordService {
    private final TemporaryPasswordRepository repository;
}
