package com.tracker.service;

import com.tracker.dto.AssignmentForm;
import com.tracker.dto.AssignmentView;
import com.tracker.dto.RosterRow;
import com.tracker.dto.StudentStats;
import com.tracker.dto.TeacherAssignmentView;
import com.tracker.model.Assignment;
import com.tracker.model.AssignmentProgress;
import com.tracker.model.ProgressStatus;
import com.tracker.model.Role;
import com.tracker.model.User;
import com.tracker.repository.AssignmentProgressRepository;
import com.tracker.repository.AssignmentRepository;
import com.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final AssignmentProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final ZoneId zone;

    public AssignmentService(AssignmentRepository assignmentRepository,
                             AssignmentProgressRepository progressRepository,
                             UserRepository userRepository,
                             @Value("${app.timezone:Asia/Kolkata}") String timezone) {
        this.assignmentRepository = assignmentRepository;
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.zone = ZoneId.of(timezone);
    }

    private LocalDate today() {
        return LocalDate.now(zone);
    }

    private static ProgressStatus statusFor(int percent) {
        if (percent <= 0) {
            return ProgressStatus.PENDING;
        }
        if (percent >= 100) {
            return ProgressStatus.COMPLETED;
        }
        return ProgressStatus.IN_PROGRESS;
    }

    // ------------------------------------------------------------------
    // Student side
    // ------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<AssignmentView> getStudentViews(User student) {
        List<Assignment> all = new ArrayList<>(assignmentRepository.findByPersonalFalseOrderByDueDateAsc());
        all.addAll(assignmentRepository.findByOwnerAndPersonalTrueOrderByDueDateAsc(student));
        all.sort(Comparator.comparing(Assignment::getDueDate));

        Map<Long, AssignmentProgress> progressByAssignment = progressRepository.findByStudent(student).stream()
                .collect(Collectors.toMap(p -> p.getAssignment().getId(), Function.identity()));

        LocalDate today = today();
        List<AssignmentView> views = new ArrayList<>();
        for (Assignment assignment : all) {
            AssignmentProgress progress = progressByAssignment.get(assignment.getId());
            if (progress != null && progress.isHidden()) {
                continue;
            }
            ProgressStatus status = progress == null ? ProgressStatus.PENDING : progress.getStatus();
            int percent = progress == null ? 0 : progress.getProgressPercent();
            boolean overdue = status != ProgressStatus.COMPLETED && assignment.getDueDate().isBefore(today);
            views.add(new AssignmentView(assignment, status, percent, overdue));
        }
        return views;
    }

    public StudentStats buildStats(List<AssignmentView> views) {
        int pending = 0;
        int inProgress = 0;
        int completed = 0;
        int overdue = 0;
        for (AssignmentView view : views) {
            switch (view.getStatus()) {
                case PENDING -> pending++;
                case IN_PROGRESS -> inProgress++;
                case COMPLETED -> completed++;
            }
            if (view.isOverdue()) {
                overdue++;
            }
        }
        return new StudentStats(views.size(), pending, inProgress, completed, overdue);
    }

    public List<AssignmentView> filter(List<AssignmentView> views, String status, String query) {
        String statusFilter = status == null ? "all" : status;
        String q = query == null ? "" : query.trim().toLowerCase();
        return views.stream()
                .filter(v -> switch (statusFilter) {
                    case "pending" -> v.getStatus() == ProgressStatus.PENDING;
                    case "in_progress" -> v.getStatus() == ProgressStatus.IN_PROGRESS;
                    case "completed" -> v.getStatus() == ProgressStatus.COMPLETED;
                    case "overdue" -> v.isOverdue();
                    default -> true;
                })
                .filter(v -> q.isEmpty()
                        || contains(v.getAssignment().getTitle(), q)
                        || contains(v.getAssignment().getSubject(), q)
                        || contains(v.getAssignment().getTeacherDisplayName(), q))
                .collect(Collectors.toList());
    }

    private static boolean contains(String value, String lowerQuery) {
        return value != null && value.toLowerCase().contains(lowerQuery);
    }

    @Transactional
    public void updateProgress(User student, Long assignmentId, int percent) {
        Assignment assignment = getAccessibleAssignment(student, assignmentId);
        AssignmentProgress progress = getOrCreateProgress(assignment, student);
        int clamped = Math.max(0, Math.min(100, percent));
        progress.setProgressPercent(clamped);
        progress.setStatus(statusFor(clamped));
        progress.setUpdatedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    @Transactional
    public void removeForStudent(User student, Long assignmentId) {
        Assignment assignment = getAccessibleAssignment(student, assignmentId);
        if (assignment.isPersonal()) {
            progressRepository.deleteByAssignment(assignment);
            assignmentRepository.delete(assignment);
        } else {
            AssignmentProgress progress = getOrCreateProgress(assignment, student);
            progress.setHidden(true);
            progress.setUpdatedAt(LocalDateTime.now());
            progressRepository.save(progress);
        }
    }

    @Transactional
    public void createPersonalAssignment(User student, AssignmentForm form) {
        Assignment assignment = new Assignment();
        assignment.setTitle(form.getTitle().trim());
        assignment.setSubject(form.getSubject().trim());
        assignment.setDescription(blankToNull(form.getDescription()));
        assignment.setTeacherName(blankToNull(form.getTeacherName()));
        assignment.setDueDate(form.getDueDate());
        assignment.setOwner(student);
        assignment.setPersonal(true);
        assignmentRepository.save(assignment);
    }

    private Assignment getAccessibleAssignment(User student, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found."));
        if (assignment.isPersonal()
                && (assignment.getOwner() == null || !assignment.getOwner().getId().equals(student.getId()))) {
            throw new AccessDeniedException("You do not have access to this assignment.");
        }
        return assignment;
    }

    private AssignmentProgress getOrCreateProgress(Assignment assignment, User student) {
        return progressRepository.findByAssignmentAndStudent(assignment, student).orElseGet(() -> {
            AssignmentProgress created = new AssignmentProgress();
            created.setAssignment(assignment);
            created.setStudent(student);
            return created;
        });
    }

    // ------------------------------------------------------------------
    // Teacher side
    // ------------------------------------------------------------------

    public void validateDueDate(AssignmentForm form, BindingResult result) {
        if (!result.hasFieldErrors("dueDate") && form.getDueDate() != null
                && form.getDueDate().isBefore(today())) {
            result.rejectValue("dueDate", "past", "Due date cannot be in the past.");
        }
    }

    @Transactional(readOnly = true)
    public List<TeacherAssignmentView> getTeacherViews(User teacher) {
        long totalStudents = userRepository.countByRole(Role.STUDENT);
        LocalDate today = today();
        List<TeacherAssignmentView> result = new ArrayList<>();
        for (Assignment assignment : assignmentRepository.findByTeacherOrderByDueDateAsc(teacher)) {
            List<AssignmentProgress> records = progressRepository.findByAssignment(assignment);
            long completed = records.stream().filter(p -> p.getStatus() == ProgressStatus.COMPLETED).count();
            long inProgress = records.stream().filter(p -> p.getStatus() == ProgressStatus.IN_PROGRESS).count();
            result.add(new TeacherAssignmentView(assignment, totalStudents, completed, inProgress,
                    assignment.getDueDate().isBefore(today)));
        }
        return result;
    }

    public Assignment getTeacherAssignment(User teacher, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found."));
        if (assignment.getTeacher() == null || !assignment.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("You do not have access to this assignment.");
        }
        return assignment;
    }

    @Transactional
    public void createTeacherAssignment(User teacher, AssignmentForm form) {
        Assignment assignment = new Assignment();
        applyForm(assignment, form);
        assignment.setTeacher(teacher);
        assignment.setPersonal(false);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public void updateTeacherAssignment(User teacher, Long assignmentId, AssignmentForm form) {
        Assignment assignment = getTeacherAssignment(teacher, assignmentId);
        applyForm(assignment, form);
        assignmentRepository.save(assignment);
    }

    @Transactional
    public void deleteTeacherAssignment(User teacher, Long assignmentId) {
        Assignment assignment = getTeacherAssignment(teacher, assignmentId);
        progressRepository.deleteByAssignment(assignment);
        assignmentRepository.delete(assignment);
    }

    public AssignmentForm toForm(Assignment assignment) {
        AssignmentForm form = new AssignmentForm();
        form.setTitle(assignment.getTitle());
        form.setSubject(assignment.getSubject());
        form.setDescription(assignment.getDescription());
        form.setDueDate(assignment.getDueDate());
        return form;
    }

    @Transactional(readOnly = true)
    public List<RosterRow> getRoster(Assignment assignment) {
        Map<Long, AssignmentProgress> byStudent = progressRepository.findByAssignment(assignment).stream()
                .collect(Collectors.toMap(p -> p.getStudent().getId(), Function.identity()));
        List<RosterRow> rows = new ArrayList<>();
        for (User student : userRepository.findByRoleOrderByFullNameAsc(Role.STUDENT)) {
            AssignmentProgress progress = byStudent.get(student.getId());
            rows.add(new RosterRow(student,
                    progress == null ? ProgressStatus.PENDING : progress.getStatus(),
                    progress == null ? 0 : progress.getProgressPercent()));
        }
        return rows;
    }

    /** Progress of one student across the assignments published by one teacher. */
    @Transactional(readOnly = true)
    public List<AssignmentView> getStudentProgressForTeacher(User teacher, User student) {
        Map<Long, AssignmentProgress> byAssignment = progressRepository.findByStudent(student).stream()
                .collect(Collectors.toMap(p -> p.getAssignment().getId(), Function.identity()));
        LocalDate today = today();
        List<AssignmentView> views = new ArrayList<>();
        for (Assignment assignment : assignmentRepository.findByTeacherOrderByDueDateAsc(teacher)) {
            AssignmentProgress progress = byAssignment.get(assignment.getId());
            ProgressStatus status = progress == null ? ProgressStatus.PENDING : progress.getStatus();
            int percent = progress == null ? 0 : progress.getProgressPercent();
            boolean overdue = status != ProgressStatus.COMPLETED && assignment.getDueDate().isBefore(today);
            views.add(new AssignmentView(assignment, status, percent, overdue));
        }
        return views;
    }

    private void applyForm(Assignment assignment, AssignmentForm form) {
        assignment.setTitle(form.getTitle().trim());
        assignment.setSubject(form.getSubject().trim());
        assignment.setDescription(blankToNull(form.getDescription()));
        assignment.setDueDate(form.getDueDate());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}