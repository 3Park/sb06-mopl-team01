package org.example.mopl.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mopl.user.entity.basic.BasicUserEntity;

@Getter
@Setter
@Entity
@Table(name = "temporary_password")
@NoArgsConstructor
public class TemporaryPassword extends BasicUserEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "password", nullable = false, length = Integer.MAX_VALUE)
    private String password;

    @Builder
    public TemporaryPassword(User user, String password) {
        this.user = user;
        this.password = password;
    }
}