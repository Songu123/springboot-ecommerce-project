package com.son.ecommerce.controller;

import com.son.ecommerce.dto.CustomUserDetails;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Show user profile page
     */
    @GetMapping
    public String profile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("success") String success,
            @ModelAttribute("error") String error,
            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findById(userDetails.getId());
        model.addAttribute("user", user);
        model.addAttribute("content", "profile/view");
        return "layout/main";
    }

    /**
     * Show edit profile form
     */
    @GetMapping("/edit")
    public String editForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("error") String error,
            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findById(userDetails.getId());
        model.addAttribute("user", user);
        model.addAttribute("content", "profile/edit");
        return "layout/main";
    }

    /**
     * Update user profile
     */
    @PostMapping("/update")
    public String updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User currentUser = userService.findById(userDetails.getId());
            currentUser.setFullName(fullName);
            currentUser.setEmail(email);
            currentUser.setPhone(phone);
            currentUser.setAddress(address);
            userService.save(currentUser);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin thành công!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật thông tin: " + e.getMessage());
            return "redirect:/profile/edit";
        }
    }

    /**
     * Show change password form
     */
    @GetMapping("/change-password")
    public String changePasswordForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @ModelAttribute("error") String error,
            @ModelAttribute("success") String success,
            Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findById(userDetails.getId());
        model.addAttribute("user", user);
        model.addAttribute("content", "profile/change-password");
        return "layout/main";
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    public String changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            User currentUser = userService.findById(userDetails.getId());
            
            // Verify current password
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
                return "redirect:/profile/change-password";
            }

            // Check if new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không khớp!");
                return "redirect:/profile/change-password";
            }

            // Check password length
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Mật khẩu phải từ 6 ký tự trở lên!");
                return "redirect:/profile/change-password";
            }

            // Update password
            currentUser.setPassword(passwordEncoder.encode(newPassword));
            userService.save(currentUser);

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi đổi mật khẩu: " + e.getMessage());
            return "redirect:/profile/change-password";
        }
    }
}


