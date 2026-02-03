package org.example.mopl.user.repository;

import org.example.mopl.user.entity.TemporaryPassword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface TemporaryPasswordRepository extends JpaRepository<TemporaryPassword, Long> {

    @Query("""
            select distinct p from TemporaryPassword p
                where p.user.email = :email
            """
    )
    Optional<TemporaryPassword> findByUserEmail(String email);

    @Modifying
    @Query("""
        delete from TemporaryPassword p
                where p.user.email = :email
        """)
    void deleteByUserEmail(String email);
}
