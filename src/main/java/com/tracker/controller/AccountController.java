package com.tracker.controller;

import com.tracker.dto.PasswordForm;
import com.tracker.model.User;
import com.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Objects;

@Controller
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/account")
    public String account(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        if (user.isAdmin()) {
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("passwordForm", new PasswordForm());
        return "account";
    }

    @PostMapping("/account/password")
    public String changePassword(@Valid @ModelAttribute("passwordForm") PasswordForm form,
                                 BindingResult result, Principal principal, RedirectAttributes redirect) {
        User user = userService.getByEmail(principal.getName());
        if (user.isAdmin()) {
            return "redirect:/admin/dashboard";
        }
        if (!result.hasFieldErrors("currentPassword") && !userService.passwordMatches(user, form.getCurrentPassword())) {
            result.rejectValue("currentPassword", "incorrect", "The current password is incorrect.");
        }
        if (!result.hasFieldErrors("newPassword") && !result.hasFieldErrors("confirmPassword")
                && !Objects.equals(form.getNewPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (result.hasErrors()) {
            return "account";
        }
        userService.changePassword(user, form.getNewPassword());
        redirect.addFlashAttribute("success", "Your password has been updated.");
        return "redirect:/account";
    }
}