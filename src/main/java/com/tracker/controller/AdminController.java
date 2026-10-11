package com.tracker.controller;

import com.tracker.model.ApprovalStatus;
import com.tracker.model.ContactMessage;
import com.tracker.model.Role;
import com.tracker.repository.AssignmentRepository;
import com.tracker.repository.ContactMessageRepository;
import com.tracker.repository.UserRepository;
import com.tracker.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final AssignmentRepository assignmentRepository;
    private final ContactMessageRepository contactRepository;

    public AdminController(UserRepository userRepository, UserService userService,
                           AssignmentRepository assignmentRepository,
                           ContactMessageRepository contactRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.assignmentRepository = assignmentRepository;
        this.contactRepository = contactRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("studentCount", userRepository.countByRole(Role.STUDENT));
        model.addAttribute("teacherCount",
                userRepository.countByRoleAndApprovalStatus(Role.TEACHER, ApprovalStatus.APPROVED));
        model.addAttribute("pendingCount",
                userRepository.countByRoleAndApprovalStatus(Role.TEACHER, ApprovalStatus.PENDING));
        model.addAttribute("assignmentCount", assignmentRepository.count());
        model.addAttribute("openMessages", contactRepository.countByResolvedFalse());
        model.addAttribute("pendingTeachers",
                userRepository.findByRoleAndApprovalStatusOrderByCreatedAtAsc(Role.TEACHER, ApprovalStatus.PENDING));
        model.addAttribute("recentMessages", contactRepository.findTop5ByOrderByCreatedAtDesc());
        return "admin/dashboard";
    }

    // ---- Teacher approvals ----

    @GetMapping("/teachers")
    public String teachers(Model model) {
        model.addAttribute("teachers", userRepository.findByRoleOrderByCreatedAtDesc(Role.TEACHER));
        return "admin/teachers";
    }

    @PostMapping("/teachers/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(defaultValue = "teachers") String returnTo,
                          RedirectAttributes redirect) {
        userService.updateTeacherStatus(id, ApprovalStatus.APPROVED);
        redirect.addFlashAttribute("success", "Teacher approved. They can now sign in.");
        return redirectTo(returnTo);
    }

    @PostMapping("/teachers/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(defaultValue = "teachers") String returnTo,
                         RedirectAttributes redirect) {
        userService.updateTeacherStatus(id, ApprovalStatus.REJECTED);
        redirect.addFlashAttribute("success", "Teacher access has been rejected.");
        return redirectTo(returnTo);
    }

    private String redirectTo(String returnTo) {
        return "dashboard".equals(returnTo) ? "redirect:/admin/dashboard" : "redirect:/admin/teachers";
    }

    // ---- Students ----

    @GetMapping("/students")
    public String students(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("students", userService.searchStudents(q));
        model.addAttribute("q", q);
        return "admin/students";
    }

    // ---- Contact messages ----

    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("messages", contactRepository.findAllByOrderByCreatedAtDesc());
        return "admin/messages";
    }

    @PostMapping("/messages/{id}/resolve")
    public String resolve(@PathVariable Long id, RedirectAttributes redirect) {
        ContactMessage message = contactRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found."));
        message.setResolved(!message.isResolved());
        contactRepository.save(message);
        redirect.addFlashAttribute("success", message.isResolved() ? "Message marked as resolved." : "Message reopened.");
        return "redirect:/admin/messages";
    }

    @PostMapping("/messages/{id}/delete")
    public String deleteMessage(@PathVariable Long id, RedirectAttributes redirect) {
        contactRepository.deleteById(id);
        redirect.addFlashAttribute("success", "Message deleted.");
        return "redirect:/admin/messages";
    }
}