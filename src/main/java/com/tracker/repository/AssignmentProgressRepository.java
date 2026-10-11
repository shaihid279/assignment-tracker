package com.tracker.repository;

import com.tracker.model.Assignment;
import com.tracker.model.AssignmentProgress;
import com.tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssignmentProgressRepository extends JpaRepository<AssignmentProgress, Long> {

    Optional<AssignmentProgress> findByAssignmentAndStudent(Assignment assignment, User student);

    List<AssignmentProgress> findByStudent(User student);

    List<AssignmentProgress> findByAssignment(Assignment assignment);

    void deleteByAssignment(Assignment assignment);
}