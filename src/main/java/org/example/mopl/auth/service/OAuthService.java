package org.example.mopl.auth.service;

import lombok.RequiredArgsConstructor;
import org.example.mopl.auth.enums.OAuthType;
import org.example.mopl.auth.event.OAuthUserCreateEvent;
import org.example.mopl.auth.port.adapter.AuthAdapter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final AuthAdapter authAdapter;
    private final ApplicationEventPublisher publisher;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuthType type = OAuthType.KAKAO;
        if(userRequest.getClientRegistration().getRegistrationId().equalsIgnoreCase("google")) {
            type = OAuthType.GOOGLE;
        }

        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
        Map<String,Object> attributes = oAuth2User.getAttributes();

        OAuth2User user = null;

        if(type == OAuthType.GOOGLE) {
            user =  googleLogin(attributes);
        }

        user = kakaoLogin(attributes);

        String email = getValue(user.getAttributes(), "email");
        if(authAdapter.blockedUser(email))
        {
            OAuth2Error error = new OAuth2Error("invalid_user","허용되지 않은 사용자 입니다.",null);
            throw new OAuth2AuthenticationException(error);
        }

        return user;
    }

    private DefaultOAuth2User googleLogin(Map<String,Object> attributes) {
        String email = getValue(attributes, "email");
        String name = getValue(attributes, "name");

        if(StringUtils.hasText(email) == false){
            email = getUniqueId(attributes, OAuthType.GOOGLE) + "@OAuthGmail.com";
        }

        if(StringUtils.hasText(name) == false){
            name = getUniqueId(attributes, OAuthType.GOOGLE) + "_gmail_user";
        }

        if(authAdapter.exsistUser(email))
        {
            publisher.publishEvent(OAuthUserCreateEvent.builder()
                    .email(email)
                    .name(name).build());
        }

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "sub"
        );
    }

    private DefaultOAuth2User kakaoLogin(Map<String,Object> attributes) {
        String name = getKakaoNickName(attributes);
        if(StringUtils.hasText(name) == false){
            name = getUniqueId(attributes, OAuthType.KAKAO) + "_kakao_user";
        }

        String email = getUniqueId(attributes, OAuthType.KAKAO) + "@OAuthKakao.com";

        if(authAdapter.exsistUser(email))
        {
            publisher.publishEvent(OAuthUserCreateEvent.builder()
                    .email(email)
                    .name(name).build());
        }

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", getUniqueId(attributes, OAuthType.KAKAO));
        map.put("email", email);
        map.put("name", name);

        return new DefaultOAuth2User(
                Set.of(new SimpleGrantedAuthority("ROLE_USER")),
                map,
                "id"
        );
    }

    private String getUniqueId(Map<String,Object> attributes, OAuthType type)
    {
        if(type == OAuthType.GOOGLE && attributes.get("sub") != null)
        {
            return attributes.get("sub").toString();

        }else if (type == OAuthType.KAKAO && attributes.get("id") != null)
        {
            return attributes.get("id").toString();
        }

        return UUID.randomUUID().toString();
    }

    private String getValue(Map<String,Object> attributes, String key)
    {
        if(attributes == null || attributes.isEmpty()
        || attributes.containsKey(key) == false)
        {
            return "";
        }

        return (String) attributes.get(key);
    }

    private String getKakaoNickName(Map<String,Object> attributes)
    {
        if(attributes == null || attributes.isEmpty()
                || attributes.containsKey("properties") == false
                || attributes.get("properties") == null
                || attributes.get("properties") instanceof Map == false
        )
        {
            return "";
        }

        LinkedHashMap<String,Object> map = (LinkedHashMap<String,Object>)attributes.get("properties");
        if(map.containsKey("nickname") == false)
            return "";

        return (String) map.get("nickname");
    }
}
