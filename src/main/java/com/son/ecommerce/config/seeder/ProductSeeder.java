package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.Category;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.repository.CategoryRepository;
import com.son.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class ProductSeeder implements BaseSeeder {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void seed() {
        System.out.println(">>> ProductSeeder is running...");

        if (productRepository.count() > 0) {
            System.out.println(">>> Products already exist. Skipping seeding.");
            return;
        }

        // Lấy tất cả categories
        List<Category> categories = categoryRepository.findAll();
        if (categories.isEmpty()) {
            System.err.println(">>> No categories found. Please run CategorySeeder first.");
            return;
        }

        Faker faker = new Faker(Locale.forLanguageTag("vi"));
        List<Product> products = new ArrayList<>();
        Random random = new Random();

        // Tạo 50 products ngẫu nhiên
        for (int i = 0; i < 50; i++) {
            // Random category
            Category randomCategory = categories.get(random.nextInt(categories.size()));

            String productName = faker.commerce().productName();
            double price = Double.parseDouble(faker.commerce().price(10000, 100000000).replace(",", ""));
            int quantity = random.nextInt(100) + 1;
            String image = "product-" + (i + 1) + ".jpg";
            String description = faker.lorem().paragraph(3);

            products.add(
                    Product.builder()
                            .name(productName)
                            .price(price)
                            .quantity(quantity)
                            .image(image)
                            .description(description)
                            .category(randomCategory)
                            .build()
            );
        }

        try {
            productRepository.saveAll(products);
            System.out.println(">>> Seeded " + products.size() + " products successfully.");
        } catch (Exception e) {
            System.err.println("Error seeding products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return 3; // Chạy sau UserSeeder (cần categories phải có sẵn)
    }
}

