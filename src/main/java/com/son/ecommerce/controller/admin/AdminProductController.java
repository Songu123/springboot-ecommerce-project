package com.son.ecommerce.controller.admin;

import com.son.ecommerce.entity.Product;
import com.son.ecommerce.service.ProductService;
import com.son.ecommerce.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/products/";

    public AdminProductController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.findAll());
        model.addAttribute("content", "admin/product/list");
        return "admin-layout";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("content", "admin/product/form");
        return "admin-layout";
    }

    // 3. SAVE
    @PostMapping("/save")
    public String save(@ModelAttribute Product product,
                      @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                      RedirectAttributes redirectAttributes) {
        try {
            // Xử lý upload file nếu có
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = uploadFile(imageFile);
                if (fileName != null) {
                    product.setImage("/uploads/products/" + fileName);
                }
            }

            // Nếu không upload file mà URL trống, giữ ảnh cũ
            if (product.getId() != null && (imageFile == null || imageFile.isEmpty()) &&
                (product.getImage() == null || product.getImage().isEmpty())) {
                Product oldProduct = productService.findById(product.getId());
                product.setImage(oldProduct.getImage());
            }

            productService.save(product);
            redirectAttributes.addFlashAttribute("success",
                "Sản phẩm '" + product.getName() + "' đã được " +
                (product.getId() != null ? "cập nhật" : "tạo") + " thành công!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    // 4. EDIT FORM
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = productService.findById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("content", "admin/product/form");
        return "admin-layout";
    }

    // 5. DELETE
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        productService.deleteById(id);
        return "redirect:/admin/products";
    }

    // Upload file method
    private String uploadFile(MultipartFile file) {
        try {
            // Tạo thư mục nếu chưa tồn tại
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Kiểm tra loại file
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Chỉ chấp nhận file ảnh!");
            }

            // Lấy extension từ file
            String originalName = file.getOriginalFilename();
            String extension = originalName != null ?
                originalName.substring(originalName.lastIndexOf(".")) : ".jpg";

            // Tạo tên file mới với UUID để tránh trùng
            String newFileName = UUID.randomUUID().toString() + extension;

            // Lưu file
            Path filePath = Paths.get(UPLOAD_DIR + newFileName);
            Files.write(filePath, file.getBytes());

            return newFileName;
        } catch (Exception e) {
            System.err.println("Error uploading file: " + e.getMessage());
            return null;
        }
    }

}
