package com.niyamitra.task.repository;

import com.niyamitra.common.enums.TaskStatus;
import com.niyamitra.task.model.ComplianceTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<ComplianceTask, UUID> {

    List<ComplianceTask> findByTenantId(UUID tenantId);

    List<ComplianceTask> findByTenantIdAndStatus(UUID tenantId, TaskStatus status);

    List<ComplianceTask> findByAssignedTo(UUID userId);

    @Query("SELECT t FROM ComplianceTask t WHERE t.tenantId = :tenantId AND t.dueDate BETWEEN :start AND :end AND t.status = 'PENDING'")
    List<ComplianceTask> findUpcomingTasks(@Param("tenantId") UUID tenantId,
                                           @Param("start") LocalDate start,
                                           @Param("end") LocalDate end);

    @Query("SELECT t FROM ComplianceTask t WHERE t.dueDate = :date AND t.status = 'PENDING' AND t.acknowledged = false")
    List<ComplianceTask> findTasksDueOn(@Param("date") LocalDate date);

    long countByTenantId(UUID tenantId);

    long countByTenantIdAndStatus(UUID tenantId, TaskStatus status);
}
