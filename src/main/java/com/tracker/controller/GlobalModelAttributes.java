package com.tracker.controller;

import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

/** Makes the signed-in user available to every template. */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        if (principal == null) {
            return null;
        }
        return userRepository.findByEmailIgnoreCase(principal.getName()).orElse(null);
    }

    @ModelAttribute("dashboardUrl")
    public String dashboardUrl(Principal principal) {
        User user = currentUser(principal);
        if (user == null) {
            return "/login";
        }
        if (user.isAdmin()) {
            return "/admin/dashboard";
        }
        return user.isTeacher() ? "/teacher/dashboard" : "/student/dashboard";
    }
}