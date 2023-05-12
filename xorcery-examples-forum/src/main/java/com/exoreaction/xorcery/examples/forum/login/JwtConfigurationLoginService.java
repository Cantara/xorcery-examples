package com.exoreaction.xorcery.examples.forum.login;

import jakarta.servlet.ServletRequest;
import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.jvnet.hk2.annotations.Service;

@Service
public class JwtConfigurationLoginService
        implements LoginService {
    private IdentityService identityService;

    @Override
    public String getName() {
        return "JwtConfigurationLoginService";
    }

    @Override
    public UserIdentity login(String username, Object credentials, ServletRequest request) {
        return null;
    }

    @Override
    public boolean validate(UserIdentity user) {
        return true;
    }

    @Override
    public IdentityService getIdentityService() {
        return identityService;
    }

    @Override
    public void setIdentityService(IdentityService service) {
        this.identityService = service;
    }

    @Override
    public void logout(UserIdentity user) {

    }
}
