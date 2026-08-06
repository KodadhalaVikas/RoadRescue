package com.roadrescue.controller;

import com.roadrescue.model.HelpRequest;
import com.roadrescue.model.User;
import com.roadrescue.repository.UserRepository;
import com.roadrescue.service.HelpRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final HelpRequestService helpRequestService;
    private final UserRepository userRepository;

    @Autowired
    public CustomerController(HelpRequestService helpRequestService, UserRepository userRepository) {
        this.helpRequestService = helpRequestService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null || !"CUSTOMER".equals(session.getAttribute("userRole"))) {
            return "redirect:/login";
        }
        User customer = userRepository.findById(userId).orElseThrow();
        List<HelpRequest> history = helpRequestService.getCustomerHistory(userId);
        model.addAttribute("customer", customer);
        model.addAttribute("history", history);
        return "customer-dashboard";
    }

    @PostMapping("/request")
    public String createRequest(HttpSession session,
                                 @RequestParam double lat,
                                 @RequestParam double lng,
                                 @RequestParam(required = false) String issue) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        User customer = userRepository.findById(userId).orElseThrow();
        HelpRequest request = helpRequestService.createRequest(customer, lat, lng, issue);
        return "redirect:/track/" + request.getId();
    }
}
