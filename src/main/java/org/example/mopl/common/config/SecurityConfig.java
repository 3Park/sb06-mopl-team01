package org.example.mopl.common.config;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.jwt.handler.*;
import org.example.mopl.auth.jwt.JwtAuthenticationFilter;
import org.example.mopl.auth.provider.CustomDaoAuthenticationProvider;
import org.example.mopl.auth.service.OAuthService;
import org.example.mopl.common.config.encoder.PasswordEncoderConfig;
import org.example.mopl.user.enums.UserRoleType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
    private final JwtLoginFailureHandler jwtLoginFailureHandler;
    private final JwtLogoutHandler jwtLogoutHandler;
    private final JwtLogoutSuccessHandler jwtLogoutSuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPointHandler  customAuthenticationEntryPointHandler;
    private final SpaCsrfTokenRequestHandler  spaCsrfTokenRequestHandler;
    private final OAuthService oAuthService;
    private final OAuthSuccessHandler  oAuthSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //PJG 추후 cors 관련 허용 사이트 설정 변경 필요 있음
        http.sessionManagement(management ->
                        management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(header -> header
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; " +
                                                "script-src 'self'; " +
                                                "script-src-attr 'none'; " +
                                                "script-src-elem 'self'; " +
                                                "object-src 'none'; " +
                                                "base-uri 'none'; " +
                                                "img-src 'self' data:; " +
                                                "form-action 'self'; "  +
                                                "connect-src 'self' https: wss:; "+
                                                "frame-ancestors 'none'"
                                ))
                        .frameOptions(frame -> frame.deny())
                        .contentTypeOptions(Customizer.withDefaults())
                        .referrerPolicy(ref -> ref
                                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(spaCsrfTokenRequestHandler)
                        .ignoringRequestMatchers(
                            "/api/auth/sign-in"
                                    , "/api/auth/reset-password"
                        ).ignoringRequestMatchers(
                                request ->
                                "/api/users".equals(request.getRequestURI())
                                 && "POST".equals(request.getMethod())))
                .authorizeHttpRequests( authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(
                                        "/api/auth/csrf-token",
                                        "/api/auth/sign-in",
                                        "/api/auth/reset-password",
                                        "/api/auth/refresh",
                                        "/actuator/health",
                                        "/actuator/info",
                                        "/ws/**",
                                        "/actuator/**"
                                ).permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                                .requestMatchers(
                                        "/index.html",
                                        "/assets/**",
                                        "/favicon.*",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html").permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(
                        info -> info.userService(oAuthService))
                        .successHandler(oAuthSuccessHandler)
                        .failureHandler(jwtLoginFailureHandler)
                )
                .formLogin(x -> x
                        .loginProcessingUrl("/api/auth/sign-in")
                        .successHandler(jwtLoginSuccessHandler)
                        .failureHandler(jwtLoginFailureHandler)
                        .permitAll())
                .logout(x -> x
                        .logoutUrl("/api/auth/sign-out")
                        .addLogoutHandler(jwtLogoutHandler)
                        .logoutSuccessHandler(jwtLogoutSuccessHandler)
                        .permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                                .authenticationEntryPoint(customAuthenticationEntryPointHandler)
                                .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }



    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("ROLE_ADMIN > ROLE_USER");
    }

    @Bean
    public AuthenticationProvider registCustomDaoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        CustomDaoAuthenticationProvider provider = new CustomDaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }
}
