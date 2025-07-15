package com.phraiz.back.member.dto.response.oauth;

import java.util.Map;

public class GoogleResponse implements OAuth2Response{

    private final Map<String, Object> attributes;

    public GoogleResponse(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return attributes.get("sub").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        return name != null ? name.toString() : null;
    }
}
