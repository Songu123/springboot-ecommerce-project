package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.Category;
import com.son.ecommerce.repository.CategoryRepository;
import com.son.ecommerce.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Profile("dev")
public class CategorySeeder implements BaseSeeder{
    @Autowired
    private CategoryRepository categoryRepo;

    @Override
    public void seed() {
        System.out.println(">>> CategorySeeder is running...");

        if (categoryRepo.count() > 0) {
            System.out.println(">>> Categories already exist. Skipping seeding.");
            return;
        }

        Faker faker = new Faker(Locale.forLanguageTag("vi"));

        Set<String> usedSlugs = new HashSet<>();

        List<Category> categories = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String name = faker.commerce().department();
            String slug = SlugUtils.toSlug(name);

            // đảm bảo slug unique
            int suffix = 1;
            while (usedSlugs.contains(slug)) {
                slug = slug + "-" + suffix++;
            }
            usedSlugs.add(slug);

            categories.add(
                    Category.builder()
                            .name(name)
                            .slug(slug)
                            .build()
            );
        }

        try {
            categoryRepo.saveAll(categories);
            System.out.println(">>> Categories seeded successfully.");
        } catch (Exception e) {
            System.err.println("Error seeding categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return 1;
    }
}
