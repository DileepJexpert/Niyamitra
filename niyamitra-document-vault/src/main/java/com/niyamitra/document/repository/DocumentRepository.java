package com.niyamitra.document.repository;

import com.niyamitra.document.model.NiyamitraDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<NiyamitraDocument, UUID> {

    List<NiyamitraDocument> findByTenantId(UUID tenantId);

    List<NiyamitraDocument> findByTenantIdAndTaskId(UUID tenantId, UUID taskId);
}
