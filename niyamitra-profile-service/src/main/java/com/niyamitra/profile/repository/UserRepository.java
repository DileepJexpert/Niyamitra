package com.niyamitra.profile.repository;

import com.niyamitra.profile.model.NiyamitraUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<NiyamitraUser, UUID> {

    List<NiyamitraUser> findByTenantId(UUID tenantId);

    Optional<NiyamitraUser> findByPhone(String phone);
}
