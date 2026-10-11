package com.tracker.repository;

import com.tracker.model.Assignment;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByTeacherOrderByDueDateAsc(User teacher);

    List<Assignment> findByPersonalFalseOrderByDueDateAsc();

    List<Assignment> findByOwnerAndPersonalTrueOrderByDueDateAsc(User owner);
}