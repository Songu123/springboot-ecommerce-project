package com.son.ecommerce.controller;

import com.son.ecommerce.service.CategoryService;
import com.son.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        // Get data for homepage
        model.addAttribute("products", productService.findAll().stream().limit(8).toList());
        model.addAttribute("categories", categoryService.findAll().stream().limit(6).toList());
        model.addAttribute("content", "home");
        return "layout/main";
    }
}
