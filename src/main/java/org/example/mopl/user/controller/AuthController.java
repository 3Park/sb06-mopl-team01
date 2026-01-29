package org.example.mopl.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.jwt.JwtTokenProvider;
import org.example.mopl.common.jwt.TokenUtils;
import org.example.mopl.common.jwt.service.RefreshTokenService;
import org.example.mopl.user.dto.JwtDto;
import org.example.mopl.user.entity.UserRoleType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    //PJG 임시. 추후 변경 필요
//    @PostMapping("/test/sign-in")
//    public ResponseEntity<JwtDto> signIn(HttpServletResponse response)
//    {
//        String token = jwtTokenProvider.generateAccessToken("admin@test.com", UserRoleType.ADMIN.name());
//        String refreshToken = jwtTokenProvider.generateRefreshToken("admin@test.com", UserRoleType.ADMIN.name());
//
//        refreshTokenService.save("admin@test.com", refreshToken);
//        response.addCookie(tokenUtils.getRefreshCookie(refreshToken));
//        JwtDto dto = new JwtDto(null,token);
//        return ResponseEntity.ok(dto);
//    }
}
