package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.Category;
import com.son.ecommerce.repository.CategoryRepository;
import com.son.ecommerce.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class CategorySeeder implements BaseSeeder{
    private final CategoryRepository categoryRepo;

    @Override
    public void seed() {
        System.out.println(">>> CategorySeeder is running...");

        if (categoryRepo.count() > 0) {
            System.out.println(">>> Categories already exist. Skipping seeding.");
            return;
        }

        // Danh sách các danh mục thực tế cho e-commerce
        String[] categoryNames = {
            "Điện thoại & Phụ kiện",
            "Laptop & Máy tính bảng",
            "Máy ảnh & Quay phim",
            "Đồng hồ thông minh",
            "Thiết bị âm thanh",
            "Thiết bị Gaming",
            "Tivi & Thiết bị nghe nhìn",
            "Thiết bị văn phòng",
            "Nhà thông minh",
            "Phụ kiện công nghệ",
            "Thời trang Nam",
            "Thời trang Nữ",
            "Giày dép",
            "Túi xách & Ví",
            "Đồng hồ",
            "Trang sức",
            "Mỹ phẩm",
            "Chăm sóc da",
            "Sách & Văn phòng phẩm",
            "Thể thao & Du lịch",
            "Đồ chơi & Giải trí",
            "Mẹ & Bé",
            "Nhà cửa & Đời sống",
            "Thực phẩm & Đồ uống"
        };

        List<Category> categories = new ArrayList<>();

        for (String name : categoryNames) {
            String slug = SlugUtils.toSlug(name);
            categories.add(
                    Category.builder()
                            .name(name)
                            .slug(slug)
                            .build()
            );
        }

        try {
            categoryRepo.saveAll(categories);
            System.out.println(">>> Seeded " + categories.size() + " categories successfully.");
        } catch (Exception e) {
            System.err.println("Error seeding categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return 1; // Chạy sau RoleSeeder
    }
}
