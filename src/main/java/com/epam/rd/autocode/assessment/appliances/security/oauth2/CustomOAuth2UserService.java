package com.epam.rd.autocode.assessment.appliances.security.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            log.warn("[OAUTH2] Provider returned no email");
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("missing_email"), "OAuth2 provider returned no email");
        }

        log.debug("[OAUTH2] Loaded user from provider: {}", email);

        // Email norm
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        attributes.put("email", email.toLowerCase().trim());

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT")),
                attributes,
                "email"
        );
    }
}