package com.niyamitra.profile.repository;

import com.niyamitra.profile.model.NiyamitraTenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepository extends JpaRepository<NiyamitraTenant, UUID> {

    Optional<NiyamitraTenant> findByGstin(String gstin);

    Optional<NiyamitraTenant> findByUdyam(String udyam);

    List<NiyamitraTenant> findByNicCodeStartingWithAndStateIn(String nicCodePrefix, List<String> states);
}
