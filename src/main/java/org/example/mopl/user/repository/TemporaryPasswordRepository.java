package org.example.mopl.user.repository;

import org.example.mopl.user.entity.TemporaryPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporaryPasswordRepository extends JpaRepository<TemporaryPassword, Long> {
}
