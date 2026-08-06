package com.roadrescue.controller;

import com.roadrescue.model.RequestStatus;
import com.roadrescue.model.User;
import com.roadrescue.repository.UserRepository;
import com.roadrescue.service.HelpRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/helper")
public class HelperController {

    private final HelpRequestService helpRequestService;
    private final UserRepository userRepository;

    @Autowired
    public HelperController(HelpRequestService helpRequestService, UserRepository userRepository) {
        this.helpRequestService = helpRequestService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !"HELPER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }
        User helper = userRepository.findById(userId).orElseThrow();
        model.addAttribute("helper", helper);
        model.addAttribute("pendingRequests", helpRequestService.getPendingRequests());
        model.addAttribute("history", helpRequestService.getHelperHistory(userId));
        return "helper-dashboard";
    }

    @PostMapping("/accept/{requestId}")
    public String accept(@PathVariable Long requestId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        User helper = userRepository.findById(userId).orElseThrow();
        helpRequestService.acceptRequest(requestId, helper);
        return "redirect:/track/" + requestId;
    }

    @PostMapping("/status/{requestId}")
    public String updateStatus(@PathVariable Long requestId, @RequestParam RequestStatus status) {
        helpRequestService.updateStatus(requestId, status);
        return "redirect:/track/" + requestId;
    }
}
