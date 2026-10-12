package com.sentineltrace.repository;

import com.sentineltrace.entity.SecurityEvent;
import com.sentineltrace.entity.enums.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {

    long countByEventType(EventType eventType);

    @Query("SELECT COUNT(e) FROM SecurityEvent e WHERE e.eventType = :eventType AND e.sourceIp = :sourceIp AND e.occurredAt >= :startTime AND e.occurredAt <= :endTime")
    long countByEventTypeAndSourceIpAndOccurredAtBetween(
            @Param("eventType") EventType eventType,
            @Param("sourceIp") String sourceIp,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    @Query("SELECT e FROM SecurityEvent e WHERE e.eventType = :eventType AND e.sourceIp = :sourceIp AND e.occurredAt >= :startTime AND e.occurredAt <= :endTime ORDER BY e.occurredAt ASC")
    List<SecurityEvent> findByEventTypeAndSourceIpAndOccurredAtBetweenOrderByOccurredAtAsc(
            @Param("eventType") EventType eventType,
            @Param("sourceIp") String sourceIp,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    @Query("SELECT e FROM SecurityEvent e WHERE " +
           "(:eventType IS NULL OR e.eventType = :eventType) AND " +
           "(:sourceIp IS NULL OR LOWER(e.sourceIp) LIKE LOWER(CONCAT('%', :sourceIp, '%'))) AND " +
           "(:from IS NULL OR e.occurredAt >= :from) AND " +
           "(:to IS NULL OR e.occurredAt <= :to) " +
           "ORDER BY e.occurredAt DESC")
    Page<SecurityEvent> findWithFilters(
            @Param("eventType") EventType eventType,
            @Param("sourceIp") String sourceIp,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    List<SecurityEvent> findTop15ByOrderByOccurredAtDesc();

    @Query("SELECT e FROM SecurityEvent e WHERE e.occurredAt >= :since ORDER BY e.occurredAt ASC")
    List<SecurityEvent> findEventsSince(@Param("since") Instant since);
}
