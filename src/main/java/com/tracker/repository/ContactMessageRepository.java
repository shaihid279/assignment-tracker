package com.tracker.repository;

import com.tracker.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    List<ContactMessage> findTop5ByOrderByCreatedAtDesc();

    long countByResolvedFalse();
}