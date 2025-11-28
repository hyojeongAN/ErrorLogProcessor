package com.example.ErrorLogProcessor.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class DashboardController {

    @GetMapping("/dashboard")
    public String showDashboard() {
        return "error-dashboard";
    }
}