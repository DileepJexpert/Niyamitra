package com.niyamitra.task.controller;

import com.niyamitra.common.enums.TaskStatus;
import com.niyamitra.task.model.ComplianceTask;
import com.niyamitra.task.service.AnupalanScoreService;
import com.niyamitra.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final AnupalanScoreService anupalanScoreService;

    @PostMapping("/tasks")
    public ResponseEntity<ComplianceTask> createTask(@RequestBody ComplianceTask task) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(task));
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<ComplianceTask>> getTasks(
            @RequestParam UUID tenantId,
            @RequestParam(required = false) TaskStatus status) {
        return ResponseEntity.ok(taskService.getTasksByTenant(tenantId, status));
    }

    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<ComplianceTask> getTask(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.getTask(taskId));
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<ComplianceTask> updateStatus(
            @PathVariable UUID taskId,
            @RequestParam TaskStatus status) {
        return ResponseEntity.ok(taskService.updateStatus(taskId, status));
    }

    @PutMapping("/tasks/{taskId}/reschedule")
    public ResponseEntity<ComplianceTask> reschedule(
            @PathVariable UUID taskId,
            @RequestParam LocalDate newDate,
            @RequestParam String reason) {
        return ResponseEntity.ok(taskService.reschedule(taskId, newDate, reason));
    }

    @PostMapping("/tasks/{taskId}/acknowledge")
    public ResponseEntity<ComplianceTask> acknowledge(@PathVariable UUID taskId) {
        return ResponseEntity.ok(taskService.acknowledge(taskId));
    }

    @GetMapping("/anupalan/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(anupalanScoreService.calculateScore(tenantId));
    }

    @GetMapping("/anupalan/score")
    public ResponseEntity<Map<String, Object>> getAnupalanScore(@RequestParam UUID tenantId) {
        return ResponseEntity.ok(anupalanScoreService.calculateScore(tenantId));
    }
}
