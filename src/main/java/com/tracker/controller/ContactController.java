package com.tracker.controller;

import com.tracker.dto.ContactForm;
import com.tracker.model.ContactMessage;
import com.tracker.model.User;
import com.tracker.repository.ContactMessageRepository;
import com.tracker.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

@Controller
public class ContactController {

    private final ContactMessageRepository contactRepository;
    private final UserRepository userRepository;

    public ContactController(ContactMessageRepository contactRepository, UserRepository userRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/contact")
    public String form(Principal principal, Model model) {
        ContactForm form = new ContactForm();
        if (principal != null) {
            User user = userRepository.findByEmailIgnoreCase(principal.getName()).orElse(null);
            if (user != null) {
                form.setName(user.getFullName());
                form.setEmail(user.getEmail());
            }
        }
        model.addAttribute("form", form);
        return "contact";
    }

    @PostMapping("/contact")
    public String submit(@Valid @ModelAttribute("form") ContactForm form, BindingResult result) {
        if (result.hasErrors()) {
            return "contact";
        }
        ContactMessage message = new ContactMessage();
        message.setName(form.getName().trim());
        message.setEmail(form.getEmail().trim());
        message.setSubject(form.getSubject().trim());
        message.setMessage(form.getMessage().trim());
        contactRepository.save(message);
        return "redirect:/contact?sent";
    }
}