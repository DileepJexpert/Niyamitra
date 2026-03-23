package com.niyamitra.anupalan.service;

import com.niyamitra.anupalan.model.AnupalanRule;
import com.niyamitra.anupalan.repository.AnupalanRuleRepository;
import com.niyamitra.common.enums.ComplianceCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnupalanRuleService {

    private final AnupalanRuleRepository ruleRepository;

    public List<AnupalanRule> getApplicableRules(String nicCode, String state) {
        log.info("Fetching Anupalan rules for NIC={}, state={}", nicCode, state);
        return ruleRepository.findApplicableRules(nicCode, state);
    }

    public AnupalanRule getRuleById(UUID ruleId) {
        return ruleRepository.findById(ruleId)
                .orElseThrow(() -> new IllegalArgumentException("Anupalan rule not found: " + ruleId));
    }

    public AnupalanRule createRule(AnupalanRule rule) {
        log.info("Creating Anupalan rule: {}", rule.getName());
        return ruleRepository.save(rule);
    }

    public AnupalanRule updateRule(UUID ruleId, AnupalanRule updated) {
        AnupalanRule existing = getRuleById(ruleId);
        existing.setName(updated.getName());
        existing.setCategory(updated.getCategory());
        existing.setAuthority(updated.getAuthority());
        existing.setApplicableStates(updated.getApplicableStates());
        existing.setApplicableNicCodes(updated.getApplicableNicCodes());
        existing.setIndustryCategories(updated.getIndustryCategories());
        existing.setRenewalPeriodMonths(updated.getRenewalPeriodMonths());
        existing.setPenaltyDescription(updated.getPenaltyDescription());
        existing.setRequiredDocuments(updated.getRequiredDocuments());
        existing.setPortalUrl(updated.getPortalUrl());
        existing.setDescriptionEn(updated.getDescriptionEn());
        existing.setDescriptionHi(updated.getDescriptionHi());
        return ruleRepository.save(existing);
    }

    public List<ComplianceCategory> getCategories() {
        return List.of(ComplianceCategory.values());
    }
}
