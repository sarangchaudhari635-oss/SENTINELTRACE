package com.sentineltrace.repository;

import com.sentineltrace.entity.Incident;
import com.sentineltrace.entity.IncidentAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentAuditLogRepository extends JpaRepository<IncidentAuditLog, Long> {

    List<IncidentAuditLog> findByIncidentIdOrderByCreatedAtDesc(Long incidentId);

    List<IncidentAuditLog> findByIncidentOrderByCreatedAtAsc(Incident incident);
}
