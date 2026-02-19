package org.example.mopl.user.entity.basic;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class BasicUserUUIDEntity extends BasicUserEntity{

    @Column(name = "uuid", nullable = false, unique = true)
    protected UUID uuid;

    @LastModifiedDate
    @Column(name = "updated_at")
    protected Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
    }
}
