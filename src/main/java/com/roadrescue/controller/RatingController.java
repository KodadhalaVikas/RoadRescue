package com.roadrescue.controller;

import com.roadrescue.model.HelpRequest;
import com.roadrescue.service.HelpRequestService;
import com.roadrescue.service.RatingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RatingController {

    private final RatingService ratingService;
    private final HelpRequestService helpRequestService;

    @Autowired
    public RatingController(RatingService ratingService, HelpRequestService helpRequestService) {
        this.ratingService = ratingService;
        this.helpRequestService = helpRequestService;
    }

    @PostMapping("/rate/{requestId}")
    public String rate(@PathVariable Long requestId,
                        @RequestParam int stars,
                        @RequestParam(required = false) String review,
                        HttpSession session) {
        HelpRequest request = helpRequestService.getById(requestId);
        Long customerId = (Long) session.getAttribute("userId");
        ratingService.submitRating(requestId, request.getHelper().getId(), customerId, stars, review);
        return "redirect:/track/" + requestId;
    }
}
