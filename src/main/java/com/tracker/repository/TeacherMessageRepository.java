package com.tracker.repository;

import com.tracker.model.TeacherMessage;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherMessageRepository extends JpaRepository<TeacherMessage, Long> {

    List<TeacherMessage> findByStudentOrderByCreatedAtDesc(User student);

    List<TeacherMessage> findByTeacherOrderByCreatedAtDesc(User teacher);

    long countByTeacherAndReplyIsNull(User teacher);
}