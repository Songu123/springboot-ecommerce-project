package com.son.ecommerce.controller.admin;

import com.son.ecommerce.dto.PaginationDto;
import com.son.ecommerce.entity.Role;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.service.RoleService;
import com.son.ecommerce.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private static final int PAGE_SIZE = 10;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, RoleService roleService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.roleService = roleService;
    }

    // 1. LIST with Pagination
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page, Model model) {
        List<User> allUsers = userService.findAll();
        PaginationDto<User> pagination = new PaginationDto<>(allUsers, page, PAGE_SIZE);

        model.addAttribute("users", pagination.getContent());
        model.addAttribute("currentPage", pagination.getCurrentPage());
        model.addAttribute("totalPages", pagination.getTotalPages());
        model.addAttribute("totalItems", pagination.getTotalItems());
        model.addAttribute("pageSize", PAGE_SIZE);
        model.addAttribute("displayStartIndex", pagination.getDisplayStartIndex());
        model.addAttribute("displayEndIndex", pagination.getDisplayEndIndex());
        model.addAttribute("content", "admin/user/list");
        return "admin-layout";
    }

    // 2. CREATE FORM
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("content", "admin/user/form");
        return "admin-layout";
    }

    // 3. SAVE
    @PostMapping("/save")
    public String save(@ModelAttribute User user, @RequestParam(required = false) List<Long> roleIds) {
        // If it's a new user (no id) or password field is filled, encode the password
        if (user.getId() == null || (user.getPassword() != null && !user.getPassword().isEmpty())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // For updates without password change, keep the existing password
            User existingUser = userService.findById(user.getId());
            user.setPassword(existingUser.getPassword());
        }

        // Set roles from roleIds
        if (roleIds != null && !roleIds.isEmpty()) {
            Set<Role> selectedRoles = new HashSet<>();
            for (Long roleId : roleIds) {
                selectedRoles.add(roleService.findById(roleId));
            }
            user.setRoles(selectedRoles);
        } else {
            // Set default role if not provided
            Role userRole = roleService.findByName("ROLE_USER");
            user.setRoles(new HashSet<>(Set.of(userRole)));
        }

        userService.save(user);
        return "redirect:/admin/users";
    }

    // 4. EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        User user = userService.findById(id);
        model.addAttribute("user", user);
        model.addAttribute("allRoles", roleService.findAll());
        model.addAttribute("content", "admin/user/form");
        return "admin-layout";
    }

    // 5. DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }
}
