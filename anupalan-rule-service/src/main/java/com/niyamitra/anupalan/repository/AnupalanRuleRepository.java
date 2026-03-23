package com.niyamitra.anupalan.repository;

import com.niyamitra.anupalan.model.AnupalanRule;
import com.niyamitra.common.enums.ComplianceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AnupalanRuleRepository extends JpaRepository<AnupalanRule, UUID> {

    List<AnupalanRule> findByCategory(ComplianceCategory category);

    @Query(value = """
            SELECT * FROM anupalan_rules.anupalan_rules
            WHERE :state = ANY(string_to_array(applicable_states, ','))
            AND EXISTS (
                SELECT 1 FROM unnest(string_to_array(applicable_nic_codes, ',')) AS nic
                WHERE :nicCode LIKE nic || '%'
            )
            """, nativeQuery = true)
    List<AnupalanRule> findApplicableRules(@Param("nicCode") String nicCode, @Param("state") String state);
}
