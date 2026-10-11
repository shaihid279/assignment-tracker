package com.tracker.controller;

import com.tracker.dto.RegistrationForm;
import com.tracker.model.Role;
import com.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin/login")
    public String adminLogin() {
        return "admin-login";
    }

    @GetMapping("/register/student")
    public String studentForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register-student";
    }

    @PostMapping("/register/student")
    public String registerStudent(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result) {
        userService.validateRegistration(form, Role.STUDENT, result);
        if (result.hasErrors()) {
            return "register-student";
        }
        try {
            userService.register(form, Role.STUDENT);
        } catch (DataIntegrityViolationException ex) {
            result.reject("duplicate", "An account with these details already exists.");
            return "register-student";
        }
        return "redirect:/login?registered";
    }

    @GetMapping("/register/teacher")
    public String teacherForm(Model model) {
        model.addAttribute("form", new RegistrationForm());
        return "register-teacher";
    }

    @PostMapping("/register/teacher")
    public String registerTeacher(@Valid @ModelAttribute("form") RegistrationForm form, BindingResult result) {
        userService.validateRegistration(form, Role.TEACHER, result);
        if (result.hasErrors()) {
            return "register-teacher";
        }
        try {
            userService.register(form, Role.TEACHER);
        } catch (DataIntegrityViolationException ex) {
            result.reject("duplicate", "An account with this email address already exists.");
            return "register-teacher";
        }
        return "redirect:/login?teacherRegistered";
    }
}