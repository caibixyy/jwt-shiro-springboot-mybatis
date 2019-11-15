package com.xyy.config.jwt;

import org.apache.shiro.authc.AuthenticationToken;

public class JwtToken implements AuthenticationToken {
    /**
     * 密钥
     */
    private String base64EncodedSecretKey;

    public JwtToken(String base64EncodedSecretKey) {
        this.base64EncodedSecretKey = base64EncodedSecretKey;
    }

    @Override
    public Object getPrincipal() {
        return base64EncodedSecretKey;
    }

    @Override
    public Object getCredentials() {
        return base64EncodedSecretKey;
    }
}
