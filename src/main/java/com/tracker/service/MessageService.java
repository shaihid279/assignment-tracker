package com.tracker.service;

import com.tracker.dto.TeacherMessageForm;
import com.tracker.model.ApprovalStatus;
import com.tracker.model.Role;
import com.tracker.model.TeacherMessage;
import com.tracker.model.User;
import com.tracker.repository.TeacherMessageRepository;
import com.tracker.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final TeacherMessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(TeacherMessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public List<User> getApprovedTeachers() {
        return userRepository.findByRoleAndApprovalStatusOrderByFullNameAsc(Role.TEACHER, ApprovalStatus.APPROVED);
    }

    public List<TeacherMessage> getStudentMessages(User student) {
        return messageRepository.findByStudentOrderByCreatedAtDesc(student);
    }

    public List<TeacherMessage> getTeacherMessages(User teacher) {
        return messageRepository.findByTeacherOrderByCreatedAtDesc(teacher);
    }

    public long countAwaiting(User teacher) {
        return messageRepository.countByTeacherAndReplyIsNull(teacher);
    }

    @Transactional
    public void sendToTeacher(User student, TeacherMessageForm form) {
        User teacher = userRepository.findById(form.getTeacherId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Teacher not found."));
        if (teacher.getRole() != Role.TEACHER || teacher.getApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The selected teacher is not available.");
        }
        TeacherMessage message = new TeacherMessage();
        message.setStudent(student);
        message.setTeacher(teacher);
        message.setSubject(form.getSubject().trim());
        message.setBody(form.getBody().trim());
        messageRepository.save(message);
    }

    @Transactional
    public void reply(User teacher, Long messageId, String reply) {
        TeacherMessage message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found."));
        if (!message.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("You cannot reply to this message.");
        }
        message.setReply(reply.trim());
        message.setRepliedAt(LocalDateTime.now());
        messageRepository.save(message);
    }
}