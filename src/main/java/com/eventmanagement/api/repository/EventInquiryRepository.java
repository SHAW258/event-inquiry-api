package com.eventmanagement.api.repository;

import com.eventmanagement.api.entity.EventInquiry;
import com.eventmanagement.api.entity.EventType;
import com.eventmanagement.api.entity.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventInquiryRepository extends JpaRepository<EventInquiry, Long> {

    Page<EventInquiry> findByUserId(Long userId, Pageable pageable);

    Optional<EventInquiry> findByIdAndUserId(Long id, Long userId);

    Page<EventInquiry> findByStatus(InquiryStatus status, Pageable pageable);

    Page<EventInquiry> findByEventType(EventType eventType, Pageable pageable);

    Page<EventInquiry> findByUserIdAndStatus(Long userId, InquiryStatus status, Pageable pageable);
}
