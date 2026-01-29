package org.example.mopl.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileUpdateRequest {

    @Size(max = 100)
    private String name;

    @Size(max = 1000)
    private String profileImageUrl;
}
