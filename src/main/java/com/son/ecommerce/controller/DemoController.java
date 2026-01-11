package com.son.ecommerce.controller;

// Java Program to Illustrate DemoController Class

// Importing required classes
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

// Class
@Controller
public class DemoController {

    // Method
    @RequestMapping("/hello")
    public String helloWorld(Model model) {

        // Sending data to view (jsp page)
        String myName = "Amiya Rout";
        model.addAttribute("myNameValue", myName);

        // Just return the page name
        // No Path, no extension
        return "demo";
    }

}