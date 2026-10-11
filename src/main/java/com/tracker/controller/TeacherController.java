package com.tracker.controller;

import com.tracker.dto.AssignmentForm;
import com.tracker.model.Assignment;
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

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final UserService userService;
    private final AssignmentService assignmentService;
    private final MessageService messageService;

    public TeacherController(UserService userService, AssignmentService assignmentService,
                             MessageService messageService) {
        this.userService = userService;
        this.assignmentService = assignmentService;
        this.messageService = messageService;
    }

    private User me(Principal principal) {
        return userService.getByEmail(principal.getName());
    }

    @GetMapping("/dashboard")
    public String dashboard(Principal principal, Model model) {
        User teacher = me(principal);
        model.addAttribute("assignments", assignmentService.getTeacherViews(teacher));
        model.addAttribute("studentCount", userService.countStudents());
        model.addAttribute("awaitingReplies", messageService.countAwaiting(teacher));
        return "teacher/dashboard";
    }

    // ---- Assignments ----

    @GetMapping("/assignments/new")
    public String newAssignment(Model model) {
        model.addAttribute("form", new AssignmentForm());
        model.addAttribute("formAction", "/teacher/assignments");
        model.addAttribute("pageTitle", "Create Assignment");
        return "teacher/assignment-form";
    }

    @PostMapping("/assignments")
    public String createAssignment(@Valid @ModelAttribute("form") AssignmentForm form, BindingResult result,
                                   Principal principal, Model model, RedirectAttributes redirect) {
        assignmentService.validateDueDate(form, result);
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/teacher/assignments");
            model.addAttribute("pageTitle", "Create Assignment");
            return "teacher/assignment-form";
        }
        assignmentService.createTeacherAssignment(me(principal), form);
        redirect.addFlashAttribute("success", "Assignment published to all students.");
        return "redirect:/teacher/dashboard";
    }

    @GetMapping("/assignments/{id}")
    public String assignmentDetail(@PathVariable Long id, Principal principal, Model model) {
        Assignment assignment = assignmentService.getTeacherAssignment(me(principal), id);
        model.addAttribute("assignment", assignment);
        model.addAttribute("roster", assignmentService.getRoster(assignment));
        return "teacher/assignment-detail";
    }

    @GetMapping("/assignments/{id}/edit")
    public String editAssignment(@PathVariable Long id, Principal principal, Model model) {
        Assignment assignment = assignmentService.getTeacherAssignment(me(principal), id);
        model.addAttribute("form", assignmentService.toForm(assignment));
        model.addAttribute("formAction", "/teacher/assignments/" + id + "/edit");
        model.addAttribute("pageTitle", "Edit Assignment");
        return "teacher/assignment-form";
    }

    @PostMapping("/assignments/{id}/edit")
    public String updateAssignment(@PathVariable Long id, @Valid @ModelAttribute("form") AssignmentForm form,
                                   BindingResult result, Principal principal, Model model,
                                   RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/teacher/assignments/" + id + "/edit");
            model.addAttribute("pageTitle", "Edit Assignment");
            return "teacher/assignment-form";
        }
        assignmentService.updateTeacherAssignment(me(principal), id, form);
        redirect.addFlashAttribute("success", "Assignment updated.");
        return "redirect:/teacher/dashboard";
    }

    @PostMapping("/assignments/{id}/delete")
    public String deleteAssignment(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        assignmentService.deleteTeacherAssignment(me(principal), id);
        redirect.addFlashAttribute("success", "Assignment deleted.");
        return "redirect:/teacher/dashboard";
    }

    // ---- Student search ----

    @GetMapping("/students")
    public String students(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("students", userService.searchStudents(q));
        model.addAttribute("q", q);
        return "teacher/students";
    }

    @GetMapping("/students/{id}")
    public String studentDetail(@PathVariable Long id, Principal principal, Model model) {
        User student = userService.getById(id);
        if (!student.isStudent()) {
            return "redirect:/teacher/students";
        }
        model.addAttribute("student", student);
        model.addAttribute("rows", assignmentService.getStudentProgressForTeacher(me(principal), student));
        return "teacher/student-detail";
    }

    // ---- Messages ----

    @GetMapping("/messages")
    public String messages(Principal principal, Model model) {
        model.addAttribute("messages", messageService.getTeacherMessages(me(principal)));
        return "teacher/messages";
    }

    @PostMapping("/messages/{id}/reply")
    public String reply(@PathVariable Long id, @RequestParam String reply,
                        Principal principal, RedirectAttributes redirect) {
        if (reply == null || reply.isBlank()) {
            redirect.addFlashAttribute("error", "The reply cannot be empty.");
        } else if (reply.length() > 2000) {
            redirect.addFlashAttribute("error", "The reply must not exceed 2000 characters.");
        } else {
            messageService.reply(me(principal), id, reply);
            redirect.addFlashAttribute("success", "Your reply has been sent.");
        }
        return "redirect:/teacher/messages";
    }
}