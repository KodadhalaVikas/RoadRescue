package com.roadrescue.controller;

import com.roadrescue.model.HelpRequest;
import com.roadrescue.repository.ChatMessageRepository;
import com.roadrescue.repository.RatingRepository;
import com.roadrescue.service.HelpRequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class TrackingController {

    private final HelpRequestService helpRequestService;
    private final ChatMessageRepository chatMessageRepository;
    private final RatingRepository ratingRepository;

    @Autowired
    public TrackingController(HelpRequestService helpRequestService,
                               ChatMessageRepository chatMessageRepository,
                               RatingRepository ratingRepository) {
        this.helpRequestService = helpRequestService;
        this.chatMessageRepository = chatMessageRepository;
        this.ratingRepository = ratingRepository;
    }

    @GetMapping("/track/{id}")
    public String track(@PathVariable Long id, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        HelpRequest request = helpRequestService.getById(id);
        model.addAttribute("request", request);
        model.addAttribute("messages", chatMessageRepository.findByRequestIdOrderBySentAtAsc(id));
        model.addAttribute("existingRating", ratingRepository.findByRequestId(id).orElse(null));
        model.addAttribute("sessionUserId", session.getAttribute("userId"));
        model.addAttribute("sessionUserName", session.getAttribute("userName"));
        model.addAttribute("sessionUserRole", session.getAttribute("userRole"));
        return "track";
    }
}
