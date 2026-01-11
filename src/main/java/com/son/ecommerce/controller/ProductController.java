package com.son.ecommerce.controller;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ProductController {

    @GetMapping
    public String index(Model model) {
        model.addAttribute("title", "Product");
        model.addAttribute("content", "product/index");
        return "layout/main";
    }
}

