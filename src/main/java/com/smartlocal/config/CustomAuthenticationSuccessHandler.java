package com.smartlocal.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

/**
 * Directs users to their corresponding dashboard based on their assigned role upon login.
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            String roleName = authority.getAuthority();

            if ("ROLE_ADMIN".equals(roleName)) {
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
                return;
            } else if ("ROLE_RETAILER".equals(roleName)) {
                response.sendRedirect(request.getContextPath() + "/retailer/dashboard");
                return;
            } else if ("ROLE_CUSTOMER".equals(roleName)) {
                response.sendRedirect(request.getContextPath() + "/customer/dashboard");
                return;
            }
        }

        // Fallback default
        response.sendRedirect(request.getContextPath() + "/");
    }
}
