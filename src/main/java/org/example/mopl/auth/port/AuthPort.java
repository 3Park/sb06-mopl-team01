package org.example.mopl.auth.port;

public interface AuthPort {
    boolean exsistUser(String email);

    boolean blockedUser(String email);
}
