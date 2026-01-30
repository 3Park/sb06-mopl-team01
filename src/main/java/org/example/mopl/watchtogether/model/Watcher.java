package org.example.mopl.watchtogether.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.mopl.user.dto.UserDto;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Watcher {
    UUID id;
    String name;
    String profileImageUrl;

    public Watcher(UserDto userDto){
        this.id = userDto.getId();
        this.name = userDto.getName();
        this.profileImageUrl =userDto.getProfileImageUrl();
    }
}
