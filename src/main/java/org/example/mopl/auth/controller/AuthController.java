package org.example.mopl.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.dto.request.ResetPasswordRequest;
import org.example.mopl.auth.jwt.JwtTokenProvider;
import org.example.mopl.auth.jwt.TokenUtils;
import org.example.mopl.auth.service.AuthService;
import org.example.mopl.auth.dto.JwtDto;
import org.example.mopl.auth.dto.JwtTokenDto;
import org.example.mopl.auth.service.MailService;
import org.example.mopl.user.entity.UserRoleType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final TokenUtils tokenUtils;
    private final MailService mailService;

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String token = csrfToken.getToken();
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refreshToken(@CookieValue(value = "REFRESH_TOKEN") String refreshToken,
                                               HttpServletResponse response) {
        JwtTokenDto dto = authService.tokenRotate(refreshToken);
        response.addCookie(tokenUtils.getRefreshCookie(dto.refreshToken()));
        return ResponseEntity.status(HttpStatus.OK).body(dto.jwtDto());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request)
    {
        mailService.sendResetPasswordMail(request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
