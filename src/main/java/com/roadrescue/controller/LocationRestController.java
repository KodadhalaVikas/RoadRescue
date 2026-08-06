package com.roadrescue.controller;

import com.roadrescue.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LocationRestController {

    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public LocationRestController(UserService userService, SimpMessagingTemplate messagingTemplate) {
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/location/{requestId}")
    public Map<String, String> updateLocation(@PathVariable Long requestId,
                                                @RequestBody Map<String, Object> payload,
                                                HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        double lat = ((Number) payload.get("lat")).doubleValue();
        double lng = ((Number) payload.get("lng")).doubleValue();
        userService.updateLocation(userId, lat, lng);

        Map<String, Object> broadcast = new LinkedHashMap<>();
        broadcast.put("userId", userId);
        broadcast.put("role", session.getAttribute("userRole"));
        broadcast.put("lat", lat);
        broadcast.put("lng", lng);
        messagingTemplate.convertAndSend("/topic/location/" + requestId, broadcast);

        return Map.of("status", "ok");
    }
}
