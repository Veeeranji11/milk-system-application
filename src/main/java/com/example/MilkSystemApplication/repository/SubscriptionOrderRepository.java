package com.example.MilkSystemApplication.repository;

import com.example.MilkSystemApplication.model.SubscriptionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionOrderRepository extends JpaRepository<SubscriptionOrder, Long> {
    List<SubscriptionOrder> findByTenantIdOrderByCreatedAtDesc(String tenantId);
    Optional<SubscriptionOrder> findByOrderRef(String orderRef);
    Optional<SubscriptionOrder> findByGatewayOrderId(String gatewayOrderId);
}
