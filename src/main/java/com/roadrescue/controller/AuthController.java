package com.roadrescue.controller;

import com.roadrescue.model.Role;
import com.roadrescue.model.User;
import com.roadrescue.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/signup")
    public String signupPage(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute User user, Model model) {
        if (userService.emailExists(user.getEmail())) {
            model.addAttribute("error", "An account with that email already exists.");
            return "signup";
        }
        userService.register(user);
        model.addAttribute("success", "Account created successfully. Please log in.");
        return "login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                         HttpSession session, Model model) {
        Optional<User> userOpt = userService.authenticate(email, password);
        if (userOpt.isEmpty()) {
            model.addAttribute("error", "Invalid email or password.");
            return "login";
        }
        User user = userOpt.get();
        session.setAttribute("userId", user.getId());
        session.setAttribute("userName", user.getFullName());
        session.setAttribute("userRole", user.getRole().name());
        if (user.getRole() == Role.CUSTOMER) {
            return "redirect:/customer/dashboard";
        }
        return "redirect:/helper/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
