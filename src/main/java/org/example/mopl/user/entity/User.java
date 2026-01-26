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
@Table(name = "users")
public class User extends BasicUserUUIDEntity {

    @NotNull
    @Column(name = "email", nullable = false, length = Integer.MAX_VALUE, unique = true)
    private String email;

    @NotNull
    @Column(name = "password", nullable = false, length = Integer.MAX_VALUE)
    private String password;

    @Column(name = "locked")
    private Boolean locked = false;
}