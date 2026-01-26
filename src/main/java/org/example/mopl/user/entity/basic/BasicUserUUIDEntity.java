package org.example.mopl.user.entity.basic;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.mopl.user.entity.listener.UserEntityListener;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(UserEntityListener.class)
public class BasicUserUUIDEntity extends BasicUserEntity{

    @NotNull
    @Column(name = "uuid", nullable = false, unique = true)
    private UUID uuid;
}
