package org.example.mopl.common.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.example.mopl.common.config.property.JwtProperties;
import org.example.mopl.user.custom.CustomUserDetailService;
import org.example.mopl.user.custom.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private final JwtProperties jwtProperties;
    private JWSSigner jwsSigner;
    private JWSVerifier jwsVerifier;
    private final CustomUserDetailService customUserDetailService;

    @PostConstruct
    public void init() {
        try
        {
            byte[] keys = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
            jwsSigner = new MACSigner(keys);
            jwsVerifier = new MACVerifier(keys);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public String generateAccessToken(String userEmail, String role)
    {
        return generateToken(userEmail, role, jwtProperties.getAccessKeyExpiration());
    }

    public String generateRefreshToken(String userEmail, String role)
    {
        return generateToken(userEmail, role, jwtProperties.getRefreshKeyExpiration());
    }

    private String generateToken(String userEmail, String role, Long expiration) {
        Date now = new Date();
        Date expirationTime = new Date(now.getTime() + expiration);
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .issuer(jwtProperties.getIssuer())
                .subject(userEmail)
                .issueTime(now)
                .expirationTime(expirationTime)
                .claim("role",role)
                .build();

        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS256);
        SignedJWT signedJWT = new SignedJWT(jwsHeader,claimsSet);
        try {
            signedJWT.sign(jwsSigner);
            return signedJWT.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validateToken(String token) {
        try
        {
            parseToken(token);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public JWTClaimsSet parseToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            if(signedJWT.verify(jwsVerifier) == false)
                throw new RuntimeException("JWT verification failed");

            JWTClaimsSet set = signedJWT.getJWTClaimsSet();
            if(set.getExpirationTime() != null && set.getExpirationTime().before(new Date()))
                throw new RuntimeException("JWT expired");

            return set;
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Authentication getAuthentication(String token) {
        JWTClaimsSet claimsSet = parseToken(token);
        String userEmail = claimsSet.getSubject();
        CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername(userEmail);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
