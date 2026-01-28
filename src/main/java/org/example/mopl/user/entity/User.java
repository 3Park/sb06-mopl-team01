package org.example.mopl.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.mopl.user.entity.basic.BasicUserUUIDEntity;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
public class User extends BasicUserUUIDEntity {

    @NotNull
    @Column(name = "email", nullable = false, length = Integer.MAX_VALUE, unique = true)
    private String email;

    @NotNull
    @Column(name = "password", nullable = false, length = Integer.MAX_VALUE)
    private String password;

    @Column(name = "locked")
    private Boolean locked = false;

//PJG    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL,  orphanRemoval = true, mappedBy = "user")
//    private Profile profile;

    @OneToMany(mappedBy = "user",  fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL,  orphanRemoval = true)
    private TemporaryPassword temporaryPassword;

    @Builder
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}