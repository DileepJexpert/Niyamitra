package com.niyamitra.anupalan.controller;

import com.niyamitra.anupalan.model.AnupalanRule;
import com.niyamitra.anupalan.service.AnupalanRuleService;
import com.niyamitra.common.enums.ComplianceCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/anupalan/rules")
@RequiredArgsConstructor
public class AnupalanRuleController {

    private final AnupalanRuleService ruleService;

    @GetMapping("/applicable")
    public ResponseEntity<List<AnupalanRule>> getApplicableRules(
            @RequestParam String nicCode,
            @RequestParam String state) {
        return ResponseEntity.ok(ruleService.getApplicableRules(nicCode, state));
    }

    @GetMapping("/{ruleId}")
    public ResponseEntity<AnupalanRule> getRule(@PathVariable UUID ruleId) {
        return ResponseEntity.ok(ruleService.getRuleById(ruleId));
    }

    @PostMapping
    public ResponseEntity<AnupalanRule> createRule(@RequestBody AnupalanRule rule) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ruleService.createRule(rule));
    }

    @PutMapping("/{ruleId}")
    public ResponseEntity<AnupalanRule> updateRule(@PathVariable UUID ruleId, @RequestBody AnupalanRule rule) {
        return ResponseEntity.ok(ruleService.updateRule(ruleId, rule));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<ComplianceCategory>> getCategories() {
        return ResponseEntity.ok(ruleService.getCategories());
    }
}
