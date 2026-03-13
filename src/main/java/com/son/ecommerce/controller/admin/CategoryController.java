package com.son.ecommerce.controller.admin;

import com.son.ecommerce.entity.Category;
import org.springframework.ui.Model;
import com.son.ecommerce.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // 1. LIST
    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("content", "admin/category/list");
        return "admin-layout";
    }

    // 2. CREATE FORM
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("content", "admin/category/form");
        return "admin-layout";
    }

    // 3. SAVE
    @PostMapping("/save")
    public String save(@ModelAttribute Category category, RedirectAttributes redirectAttributes) {
        try {
            // Validation
            if (category.getName() == null || category.getName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Tên danh mục không được để trống!");
                return category.getId() == null ? "redirect:/admin/categories/create" : "redirect:/admin/categories/edit/" + category.getId();
            }

            if (category.getSlug() == null || category.getSlug().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Slug không được để trống!");
                return category.getId() == null ? "redirect:/admin/categories/create" : "redirect:/admin/categories/edit/" + category.getId();
            }

            if (!category.getSlug().matches("^[a-z0-9-]+$")) {
                redirectAttributes.addFlashAttribute("error", "Slug chỉ chứa chữ thường, số và dấu gạch ngang!");
                return category.getId() == null ? "redirect:/admin/categories/create" : "redirect:/admin/categories/edit/" + category.getId();
            }

            // Trim values
            category.setName(category.getName().trim());
            category.setSlug(category.getSlug().trim());

            Category savedCategory;
            if (category.getId() != null) {
                // Update: lấy entity cũ, chỉ cập nhật name/slug
                Category old = categoryService.findById(category.getId());
                old.setName(category.getName());
                old.setSlug(category.getSlug());
                savedCategory = categoryService.save(old);
                redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục '" + savedCategory.getName() + "' thành công!");
            } else {
                // Create mới
                savedCategory = categoryService.save(category);
                redirectAttributes.addFlashAttribute("success", "Tạo danh mục '" + savedCategory.getName() + "' thành công!");
            }

            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/categories";
        }
    }

    // 4. EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id);
            model.addAttribute("category", category);
            model.addAttribute("content", "admin/category/form");
            return "admin-layout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/admin/categories";
        }
    }

    // 5. DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id);
            String categoryName = category.getName();

            categoryService.deleteById(id);

            redirectAttributes.addFlashAttribute("success", "Xóa danh mục '" + categoryName + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa danh mục: " + e.getMessage());
        }

        return "redirect:/admin/categories";
    }

}
