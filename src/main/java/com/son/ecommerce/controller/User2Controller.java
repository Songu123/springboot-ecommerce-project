package com.son.ecommerce.controller;

import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path = "/user2")
public class User2Controller {

    @Autowired
    private UserRepository userRepository;

//    public @ResponseBody String addUser2(@RequestParam String name, @RequestParam String email) {
//        com.example.son.entity.User n = new com.example.son.entity.User();
//        n.setFullName(name);
//        n.setEmail(email);
//        userRepository.save(n);
//        return "User2 Added Successfully";
//    }

    @GetMapping(path = "/all")
    public @ResponseBody Iterable<User> getAllUsers2() {
        return userRepository.findAll();
    }
}
