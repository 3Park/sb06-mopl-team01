package org.example.mopl.watchtogether.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.user.dto.UserDto;

import java.util.UUID;

@Getter
@NoArgsConstructor
public class Watcher {
    private UUID userId;
    private String name;
    private String profileImageUrl;

    public Watcher(UserDto userDto){
        this.userId = userDto.getId();
        this.name = userDto.getName();
        this.profileImageUrl =userDto.getProfileImageUrl();
    }
}
