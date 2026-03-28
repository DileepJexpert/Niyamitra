package com.niyamitra.kavach.notification.repository;

import com.niyamitra.kavach.notification.model.KavachNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<KavachNotification, UUID> {

    List<KavachNotification> findByTenantId(UUID tenantId);

    List<KavachNotification> findByTenantIdOrderBySentAtDesc(UUID tenantId);

    List<KavachNotification> findByDeliveryStatus(String status);
}
