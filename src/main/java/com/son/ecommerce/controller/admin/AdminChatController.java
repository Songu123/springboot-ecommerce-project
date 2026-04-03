package com.son.ecommerce.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/chat")
public class AdminChatController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("pageTitle", "Hỗ trợ trực tuyến - Admin");
        model.addAttribute("content", "admin/chat/index");
        return "admin-layout";
    }
}
