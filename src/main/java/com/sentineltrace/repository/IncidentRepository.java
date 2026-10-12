package com.sentineltrace.repository;

import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.enums.IncidentStatus;
import com.sentineltrace.entity.enums.Severity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    Optional<Incident> findByFingerprintAndStatusIn(String fingerprint, Collection<IncidentStatus> statuses);

    @Query("SELECT i FROM Incident i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:severity IS NULL OR i.severity = :severity) " +
           "ORDER BY i.lastSeen DESC")
    Page<Incident> findWithFilters(
            @Param("status") IncidentStatus status,
            @Param("severity") Severity severity,
            Pageable pageable
    );

    long countByStatus(IncidentStatus status);

    long countBySeverity(Severity severity);

    long countByStatusAndSeverity(IncidentStatus status, Severity severity);

    List<Incident> findTop10ByOrderByLastSeenDesc();
}
