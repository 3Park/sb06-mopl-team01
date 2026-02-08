package org.example.mopl.auth.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthUserCreateEvent {
    private String email;
    private String name;

    @Builder
    public OAuthUserCreateEvent(String email, String name) {
        this.email = email;
        this.name = name;
    }
}


