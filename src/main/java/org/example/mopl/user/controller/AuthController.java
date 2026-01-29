package org.example.mopl.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.jwt.JwtTokenProvider;
import org.example.mopl.common.jwt.TokenUtils;
import org.example.mopl.common.jwt.service.AuthService;
import org.example.mopl.user.dto.JwtDto;
import org.example.mopl.user.dto.JwtTokenDto;
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

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refreshToken(@CookieValue(value = "REFRESH_TOKEN") String refreshToken,
                                               HttpServletResponse response) {
        JwtTokenDto dto = authService.tokenRotate(refreshToken);
        response.addCookie(tokenUtils.getRefreshCookie(dto.refreshToken()));
        return ResponseEntity.status(HttpStatus.OK).body(dto.jwtDto());
    }

    //PJG 임시. 추후 변경 필요
//    @PostMapping("/test/sign-in")
//    public ResponseEntity<JwtDto> signIn(HttpServletResponse response)
//    {
//        String token = jwtTokenProvider.generateAccessToken("admin@test.com", UserRoleType.ADMIN.name());
//        String refreshToken = jwtTokenProvider.generateRefreshToken("admin@test.com", UserRoleType.ADMIN.name());
//
//        authService.saveRefreshToken("admin@test.com", refreshToken);
//        response.addCookie(tokenUtils.getRefreshCookie(refreshToken));
//        JwtDto dto = new JwtDto(null,token);
//        return ResponseEntity.ok(dto);
//    }
}
