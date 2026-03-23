package com.niyamitra.task.service;

import com.niyamitra.common.enums.TaskStatus;
import com.niyamitra.task.model.ComplianceTask;
import com.niyamitra.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnupalanScoreService {

    private final TaskRepository taskRepository;

    /**
     * Anupalan Score = (0.4 x Overdue_Penalty) + (0.3 x Late_Submission_Pattern)
     *                + (0.2 x Pending_Tasks_Ratio) + (0.1 x Historical_Violations)
     *
     * Score ranges: 0-40 (Low Risk/Green), 41-70 (Medium/Yellow), 71-100 (High Risk/Red)
     */
    public Map<String, Object> calculateScore(UUID tenantId) {
        List<ComplianceTask> allTasks = taskRepository.findByTenantId(tenantId);
        long totalTasks = allTasks.size();

        if (totalTasks == 0) {
            return Map.of("score", 0, "riskLevel", "GREEN", "totalTasks", 0);
        }

        // Overdue penalty: ratio of overdue tasks
        long overdueTasks = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.OVERDUE ||
                        (t.getStatus() == TaskStatus.PENDING && t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now())))
                .count();
        double overduePenalty = (double) overdueTasks / totalTasks * 100;

        // Late submission pattern: avg days late for completed tasks
        double avgDaysLate = allTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.COMPLETED && t.getCompletedDate() != null && t.getDueDate() != null)
                .mapToLong(t -> Math.max(0, ChronoUnit.DAYS.between(t.getDueDate(), t.getCompletedDate())))
                .average()
                .orElse(0);
        double lateSubmissionScore = Math.min(avgDaysLate * 3.33, 100); // cap at 100

        // Pending tasks ratio
        long pendingTasks = taskRepository.countByTenantIdAndStatus(tenantId, TaskStatus.PENDING);
        double pendingRatio = (double) pendingTasks / totalTasks * 100;

        // Historical violations (escalated tasks as proxy)
        long escalatedTasks = taskRepository.countByTenantIdAndStatus(tenantId, TaskStatus.ESCALATED);
        double violationScore = Math.min(escalatedTasks * 20, 100);

        // Weighted score
        double score = (0.4 * overduePenalty) + (0.3 * lateSubmissionScore)
                      + (0.2 * pendingRatio) + (0.1 * violationScore);
        int roundedScore = (int) Math.min(Math.round(score), 100);

        String riskLevel;
        if (roundedScore <= 40) riskLevel = "GREEN";
        else if (roundedScore <= 70) riskLevel = "YELLOW";
        else riskLevel = "RED";

        log.info("Anupalan Score for tenant {}: {} ({})", tenantId, roundedScore, riskLevel);

        return Map.of(
                "score", roundedScore,
                "riskLevel", riskLevel,
                "totalTasks", totalTasks,
                "overdueTasks", overdueTasks,
                "pendingTasks", pendingTasks,
                "completedTasks", taskRepository.countByTenantIdAndStatus(tenantId, TaskStatus.COMPLETED)
        );
    }
}
