package com.tracker.service;

import com.tracker.dto.RegistrationForm;
import com.tracker.model.ApprovalStatus;
import com.tracker.model.Role;
import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
    }

    public long countStudents() {
        return userRepository.countByRole(Role.STUDENT);
    }

    /** Adds field errors to the binding result when the registration data is not acceptable. */
    public void validateRegistration(RegistrationForm form, Role role, BindingResult result) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmPassword")
                && !Objects.equals(form.getPassword(), form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (!result.hasFieldErrors("email") && form.getEmail() != null
                && userRepository.existsByEmailIgnoreCase(form.getEmail().trim())) {
            result.rejectValue("email", "duplicate", "An account with this email address already exists.");
        }
        if (isBlank(form.getDepartment())) {
            result.rejectValue("department", "required", "Department is required.");
        }
        if (role == Role.STUDENT) {
            if (isBlank(form.getRollNumber())) {
                result.rejectValue("rollNumber", "required", "Roll number is required.");
            } else if (!result.hasFieldErrors("rollNumber")
                    && userRepository.existsByRollNumberIgnoreCase(form.getRollNumber().trim())) {
                result.rejectValue("rollNumber", "duplicate", "This roll number is already registered.");
            }
            if (isBlank(form.getIdCardNumber())) {
                result.rejectValue("idCardNumber", "required", "ID card number is required.");
            } else if (!result.hasFieldErrors("idCardNumber")
                    && userRepository.existsByIdCardNumberIgnoreCase(form.getIdCardNumber().trim())) {
                result.rejectValue("idCardNumber", "duplicate", "This ID card number is already registered.");
            }
        }
    }

    @Transactional
    public User register(RegistrationForm form, Role role) {
        User user = new User();
        user.setFullName(form.getFullName().trim());
        user.setEmail(form.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(role);
        user.setDepartment(form.getDepartment().trim());
        if (role == Role.STUDENT) {
            user.setRollNumber(form.getRollNumber().trim().toUpperCase());
            user.setIdCardNumber(form.getIdCardNumber().trim().toUpperCase());
            user.setApprovalStatus(ApprovalStatus.APPROVED);
        } else {
            user.setApprovalStatus(ApprovalStatus.PENDING);
        }
        return userRepository.save(user);
    }

    public List<User> searchStudents(String query) {
        if (query == null || query.isBlank()) {
            return userRepository.findByRoleOrderByFullNameAsc(Role.STUDENT);
        }
        return userRepository.searchByRole(Role.STUDENT, query.trim());
    }

    @Transactional
    public void updateTeacherStatus(Long teacherId, ApprovalStatus status) {
        User teacher = getById(teacherId);
        if (teacher.getRole() != Role.TEACHER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected user is not a teacher.");
        }
        teacher.setApprovalStatus(status);
        userRepository.save(teacher);
    }

    public boolean passwordMatches(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPassword());
    }

    @Transactional
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}