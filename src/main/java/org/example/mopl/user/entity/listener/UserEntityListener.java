package org.example.mopl.user.entity.listener;

import jakarta.persistence.PrePersist;
import org.example.mopl.user.entity.Profile;
import org.example.mopl.user.entity.Role;
import org.example.mopl.user.entity.User;
import org.example.mopl.user.entity.basic.BasicUserUUIDEntity;

import java.util.UUID;

public class UserEntityListener {
    @PrePersist
    public void prePersist(Object entity) {
        if(entity instanceof BasicUserUUIDEntity e && e.getId() == null) {
            e.setUuid(UUID.randomUUID());
        }
    }
}
