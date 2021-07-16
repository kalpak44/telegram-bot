package com.home.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BaseController {
    @GetMapping("/success")
    public String success(Model model) {
        return "success";
    }
    @GetMapping("/cancel")
    public String cancel(Model model) {
        return "cancel";
    }

}


