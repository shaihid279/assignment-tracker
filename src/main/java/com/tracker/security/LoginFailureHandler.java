package com.tracker.security;

import com.tracker.model.ApprovalStatus;
import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;

    public LoginFailureHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        boolean adminPortal = "admin".equals(request.getParameter("portal"));
        String base = adminPortal ? "/admin/login" : "/login";
        String query = "error";

        if (exception instanceof DisabledException) {
            String email = request.getParameter("username");
            Optional<User> user = userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim());
            if (user.isPresent()) {
                query = user.get().getApprovalStatus() == ApprovalStatus.REJECTED ? "rejected" : "pending";
            }
        }
        response.sendRedirect(request.getContextPath() + base + "?" + query);
    }
}