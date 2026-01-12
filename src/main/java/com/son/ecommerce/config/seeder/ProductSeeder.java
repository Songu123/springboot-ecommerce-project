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

        // Tạo 100 products với Faker
        for (int i = 0; i < 100; i++) {
            Category randomCategory = categories.get(random.nextInt(categories.size()));

            // Sử dụng Faker để tạo tên sản phẩm động
            String productName = generateProductName(faker, randomCategory.getSlug());

            // Giá phù hợp với loại sản phẩm (dựa trên category)
            double price = generatePrice(faker, randomCategory.getSlug());
            price = Math.round(price / 1000) * 1000; // Làm tròn đến nghìn

            // Số lượng ngẫu nhiên
            int quantity = faker.number().numberBetween(1, 100);

            // Tạo tên ảnh động
            String image = faker.internet().image();

            // Mô tả sản phẩm
            String description = faker.lorem().paragraph(faker.number().numberBetween(2, 5));

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
        return 3; // Chạy sau UserSeeder
    }

    /**
     * Tạo tên sản phẩm động dựa trên category slug
     */
    private String generateProductName(Faker faker, String categorySlug) {
        String baseName = faker.commerce().productName();
        String brand = faker.company().name();
        String color = faker.color().name();
        String material = faker.commerce().material();

        // Tạo tên sản phẩm đa dạng dựa trên category
        if (categorySlug.contains("dien-thoai")) {
            return brand + " " + faker.commerce().productName() + " " + faker.number().numberBetween(8, 256) + "GB";
        } else if (categorySlug.contains("laptop")) {
            return brand + " " + baseName + " " + faker.options().option("Core i5", "Core i7", "Ryzen 5", "Ryzen 7", "M1", "M2");
        } else if (categorySlug.contains("may-anh")) {
            return brand + " " + faker.commerce().productName() + " " + faker.number().numberBetween(20, 50) + "MP";
        } else if (categorySlug.contains("dong-ho")) {
            return brand + " " + faker.options().option("Smart", "Sport", "Classic") + " Watch";
        } else if (categorySlug.contains("thoi-trang")) {
            return faker.commerce().productName() + " " + color + " " + material;
        } else if (categorySlug.contains("giay")) {
            return brand + " " + faker.options().option("Running", "Casual", "Sport", "Sneaker") + " Shoes";
        } else {
            return baseName + " " + brand;
        }
    }

    /**
     * Tạo giá sản phẩm động dựa trên category slug
     */
    private double generatePrice(Faker faker, String categorySlug) {
        if (categorySlug.contains("dien-thoai") || categorySlug.contains("laptop") || categorySlug.contains("may-anh")) {
            return faker.number().numberBetween(5000, 50000) * 1000.0; // 5-50 triệu
        } else if (categorySlug.contains("dong-ho")) {
            return faker.number().numberBetween(1000, 20000) * 1000.0; // 1-20 triệu
        } else if (categorySlug.contains("thoi-trang") || categorySlug.contains("giay")) {
            return faker.number().numberBetween(100, 3000) * 1000.0; // 100k-3 triệu
        } else if (categorySlug.contains("my-pham") || categorySlug.contains("cham-soc")) {
            return faker.number().numberBetween(50, 2000) * 1000.0; // 50k-2 triệu
        } else {
            return faker.number().numberBetween(100, 10000) * 1000.0; // Default: 100k-10 triệu
        }
    }
}
