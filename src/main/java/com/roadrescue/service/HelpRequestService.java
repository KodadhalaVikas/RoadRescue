package com.roadrescue.service;

import com.roadrescue.model.HelpRequest;
import com.roadrescue.model.RequestStatus;
import com.roadrescue.model.User;
import com.roadrescue.repository.HelpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class HelpRequestService {

    private final HelpRequestRepository helpRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public HelpRequestService(HelpRequestRepository helpRequestRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.helpRequestRepository = helpRequestRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public HelpRequest createRequest(User customer, double lat, double lng, String issue) {
        HelpRequest request = new HelpRequest();
        request.setCustomer(customer);
        request.setCustomerLat(lat);
        request.setCustomerLng(lng);
        request.setIssueDescription(issue);
        request.setStatus(RequestStatus.PENDING);
        return helpRequestRepository.save(request);
    }

    public List<HelpRequest> getPendingRequests() {
        return helpRequestRepository.findByStatusOrderByCreatedAtDesc(RequestStatus.PENDING);
    }

    public HelpRequest acceptRequest(Long requestId, User helper) {
        HelpRequest request = helpRequestRepository.findById(requestId).orElseThrow();
        request.setHelper(helper);
        request.setStatus(RequestStatus.ACCEPTED);
        request.setAcceptedAt(LocalDateTime.now());
        HelpRequest saved = helpRequestRepository.save(request);
        broadcastStatus(saved);
        return saved;
    }

    public HelpRequest updateStatus(Long requestId, RequestStatus status) {
        HelpRequest request = helpRequestRepository.findById(requestId).orElseThrow();
        request.setStatus(status);
        if (status == RequestStatus.COMPLETED) {
            request.setCompletedAt(LocalDateTime.now());
        }
        HelpRequest saved = helpRequestRepository.save(request);
        broadcastStatus(saved);
        return saved;
    }

    public List<HelpRequest> getCustomerHistory(Long customerId) {
        return helpRequestRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<HelpRequest> getHelperHistory(Long helperId) {
        return helpRequestRepository.findByHelperIdOrderByCreatedAtDesc(helperId);
    }

    public HelpRequest getById(Long id) {
        return helpRequestRepository.findById(id).orElseThrow();
    }

    /** Notifies anyone viewing /track/{id} (in either role) that the status just changed,
     *  so their page can refresh itself instead of requiring a manual reload. */
    private void broadcastStatus(HelpRequest request) {
        messagingTemplate.convertAndSend(
                "/topic/status/" + request.getId(),
                Map.of("status", request.getStatus().name())
        );
    }
}
