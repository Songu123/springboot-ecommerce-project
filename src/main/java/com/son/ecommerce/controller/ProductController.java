package com.son.ecommerce.controller;

import com.son.ecommerce.entity.Category;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.service.CategoryService;
import com.son.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    /**
     * Display all products with search, filter, and sort
     */
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sort,
            Model model) {

        List<Product> products = productService.findAll();

        // Search by name or description
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            products = products.stream()
                    .filter(p -> p.getName().toLowerCase().contains(searchLower) ||
                            (p.getDescription() != null && p.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }

        // Filter by category
        if (categoryId != null) {
            products = products.stream()
                    .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        // Filter by price range
        if (minPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() >= minPrice)
                    .collect(Collectors.toList());
        }

        if (maxPrice != null) {
            products = products.stream()
                    .filter(p -> p.getPrice() <= maxPrice)
                    .collect(Collectors.toList());
        }

        // Sort products
        if (sort != null) {
            switch (sort) {
                case "price_asc":
                    products.sort(Comparator.comparing(Product::getPrice));
                    break;
                case "price_desc":
                    products.sort(Comparator.comparing(Product::getPrice).reversed());
                    break;
                case "name_asc":
                    products.sort(Comparator.comparing(Product::getName));
                    break;
                case "name_desc":
                    products.sort(Comparator.comparing(Product::getName).reversed());
                    break;
                case "newest":
                    products.sort(Comparator.comparing(Product::getId).reversed());
                    break;
                default:
                    products.sort(Comparator.comparing(Product::getId).reversed());
            }
        } else {
            // Default sort: newest first
            products.sort(Comparator.comparing(Product::getId).reversed());
        }

        // Get all categories for filter dropdown
        List<Category> categories = categoryService.findAll();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("searchKeyword", search);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("content", "products");

        return "layout/main";
    }

    /**
     * View product detail
     */
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        try {
            Product product = productService.findById(id);

            // Get related products (same category, limit 4)
            List<Product> relatedProducts = product.getCategory() != null ?
                    productService.findByCategoryId(product.getCategory().getId()).stream()
                            .filter(p -> !p.getId().equals(id))
                            .limit(4)
                            .collect(Collectors.toList()) :
                    List.of();

            model.addAttribute("product", product);
            model.addAttribute("relatedProducts", relatedProducts);
            model.addAttribute("content", "product-detail");

            return "layout/main";
        } catch (RuntimeException e) {
            return "redirect:/products?error=Product not found";
        }
    }

    /**
     * View products by category slug
     */
    @GetMapping("/category/{slug}")
    public String productsByCategory(
            @PathVariable String slug,
            @RequestParam(required = false) String sort,
            Model model) {

        try {
            // Find category by name (temporary, should use slug field)
            List<Category> allCategories = categoryService.findAll();
            Category category = allCategories.stream()
                    .filter(c -> c.getName().toLowerCase().replace(" ", "-").equals(slug.toLowerCase()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Category not found"));

            List<Product> products = productService.findByCategoryId(category.getId());

            // Apply sorting
            if (sort != null) {
                switch (sort) {
                    case "price_asc":
                        products.sort(Comparator.comparing(Product::getPrice));
                        break;
                    case "price_desc":
                        products.sort(Comparator.comparing(Product::getPrice).reversed());
                        break;
                    case "name_asc":
                        products.sort(Comparator.comparing(Product::getName));
                        break;
                    case "newest":
                        products.sort(Comparator.comparing(Product::getId).reversed());
                        break;
                    default:
                        products.sort(Comparator.comparing(Product::getId).reversed());
                }
            }

            model.addAttribute("products", products);
            model.addAttribute("categories", allCategories);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedCategoryId", category.getId());
            model.addAttribute("totalProducts", products.size());
            model.addAttribute("selectedSort", sort);
            model.addAttribute("content", "products");

            return "layout/main";
        } catch (RuntimeException e) {
            return "redirect:/products?error=Category not found";
        }
    }
}

