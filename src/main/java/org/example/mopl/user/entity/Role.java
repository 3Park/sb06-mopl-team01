package org.example.mopl.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.mopl.user.entity.basic.BasicUserUUIDEntity;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BasicUserUUIDEntity {
    @NotNull
    @Column(name = "name", nullable = false, length = Integer.MAX_VALUE)
    private String name;

    @NotNull
    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;
}