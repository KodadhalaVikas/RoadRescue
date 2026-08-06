package com.roadrescue.repository;

import com.roadrescue.model.HelpRequest;
import com.roadrescue.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    List<HelpRequest> findByStatusOrderByCreatedAtDesc(RequestStatus status);
    List<HelpRequest> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<HelpRequest> findByHelperIdOrderByCreatedAtDesc(Long helperId);
}
