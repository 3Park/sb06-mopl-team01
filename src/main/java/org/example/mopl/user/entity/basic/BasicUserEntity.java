package org.example.mopl.user.entity.basic;

import io.hypersistence.utils.hibernate.id.Tsid;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BasicUserEntity {
    @Id
    @Tsid
    @Column(name = "id", nullable = false)
    protected Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    protected Instant createdAt;
}
