package com.niyamitra.task.model;

import com.niyamitra.common.enums.ComplianceCategory;
import com.niyamitra.common.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "compliance_tasks", schema = "niyamitra_tasks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ComplianceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "rule_id")
    private UUID ruleId;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ComplianceCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "acknowledged")
    @Builder.Default
    private boolean acknowledged = false;

    @Column(name = "escalation_level")
    @Builder.Default
    private int escalationLevel = 0;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
