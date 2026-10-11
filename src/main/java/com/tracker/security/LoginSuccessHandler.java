package com.tracker.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String contextPath = request.getContextPath();
        boolean adminPortal = "admin".equals(request.getParameter("portal"));
        boolean isAdmin = hasRole(authentication, "ROLE_ADMIN");
        boolean isTeacher = hasRole(authentication, "ROLE_TEACHER");

        if (adminPortal && !isAdmin) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.sendRedirect(contextPath + "/admin/login?denied");
            return;
        }

        String target;
        if (isAdmin) {
            target = "/admin/dashboard";
        } else if (isTeacher) {
            target = "/teacher/dashboard";
        } else {
            target = "/student/dashboard";
        }
        response.sendRedirect(contextPath + target);
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}