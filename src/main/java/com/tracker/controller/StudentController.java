package com.tracker.controller;

import com.tracker.dto.AssignmentForm;
import com.tracker.dto.AssignmentView;
import com.tracker.dto.TeacherMessageForm;
import com.tracker.model.User;
import com.tracker.service.AssignmentService;
import com.tracker.service.MessageService;
import com.tracker.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final UserService userService;
    private final AssignmentService assignmentService;
    private final MessageService messageService;

    public StudentController(UserService userService, AssignmentService assignmentService,
                             MessageService messageService) {
        this.userService = userService;
        this.assignmentService = assignmentService;
        this.messageService = messageService;
    }

    private User me(Principal principal) {
        return userService.getByEmail(principal.getName());
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "all") String status,
                            @RequestParam(required = false) String q,
                            Principal principal, Model model) {
        User student = me(principal);
        List<AssignmentView> all = assignmentService.getStudentViews(student);
        model.addAttribute("stats", assignmentService.buildStats(all));
        model.addAttribute("assignments", assignmentService.filter(all, status, q));
        model.addAttribute("status", status);
        model.addAttribute("q", q);
        return "student/dashboard";
    }

    @GetMapping("/assignments/new")
    public String newAssignment(Model model) {
        model.addAttribute("form", new AssignmentForm());
        return "student/assignment-form";
    }

    @PostMapping("/assignments")
    public String createAssignment(@Valid @ModelAttribute("form") AssignmentForm form, BindingResult result,
                                   Principal principal, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "student/assignment-form";
        }
        assignmentService.createPersonalAssignment(me(principal), form);
        redirect.addFlashAttribute("success", "Assignment added to your tracker.");
        return "redirect:/student/dashboard";
    }

    @PostMapping("/assignments/{id}/progress")
    public String updateProgress(@PathVariable Long id, @RequestParam int percent,
                                 Principal principal, RedirectAttributes redirect) {
        assignmentService.updateProgress(me(principal), id, percent);
        redirect.addFlashAttribute("success", "Progress updated.");
        return "redirect:/student/dashboard";
    }

    @PostMapping("/assignments/{id}/complete")
    public String complete(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        assignmentService.updateProgress(me(principal), id, 100);
        redirect.addFlashAttribute("success", "Assignment marked as completed.");
        return "redirect:/student/dashboard";
    }

    @PostMapping("/assignments/{id}/reopen")
    public String reopen(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        assignmentService.updateProgress(me(principal), id, 0);
        redirect.addFlashAttribute("success", "Assignment moved back to pending.");
        return "redirect:/student/dashboard";
    }

    @PostMapping("/assignments/{id}/delete")
    public String delete(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        assignmentService.removeForStudent(me(principal), id);
        redirect.addFlashAttribute("success", "Assignment removed from your list.");
        return "redirect:/student/dashboard";
    }

    @GetMapping("/contact-teacher")
    public String contactTeacher(Principal principal, Model model) {
        model.addAttribute("form", new TeacherMessageForm());
        populateMessages(me(principal), model);
        return "student/contact-teacher";
    }

    @PostMapping("/contact-teacher")
    public String sendToTeacher(@Valid @ModelAttribute("form") TeacherMessageForm form, BindingResult result,
                                Principal principal, Model model, RedirectAttributes redirect) {
        User student = me(principal);
        if (result.hasErrors()) {
            populateMessages(student, model);
            return "student/contact-teacher";
        }
        messageService.sendToTeacher(student, form);
        redirect.addFlashAttribute("success", "Your message has been sent to the teacher.");
        return "redirect:/student/contact-teacher";
    }

    private void populateMessages(User student, Model model) {
        model.addAttribute("teachers", messageService.getApprovedTeachers());
        model.addAttribute("messages", messageService.getStudentMessages(student));
    }
}