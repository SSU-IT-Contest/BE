package com.phraiz.back.member.dto.response.oauth;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User {

    private final OAuth2Response oAuth2Response;
    private final String role;

    @Override
    public Map<String, Object> getAttributes() {
        return oAuth2Response.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return role;
            }
        });
        return authorities;
    }

    @Override
    public String getName() {
        return oAuth2Response.getProviderId();
    }

    public String getUsername() {
        //return oAuth2Response.getProvider()+" "+oAuth2Response.getProviderId();
        return oAuth2Response.getProviderId();
    }
}
