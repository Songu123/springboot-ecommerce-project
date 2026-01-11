package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class UserSeeder implements BaseSeeder {
    private final UserRepository userRepository;

    @Override
    public void seed() {
        System.out.println(">>> UserSeeder is running...");

        if (userRepository.count() > 0) {
            System.out.println(">>> Users already exist. Skipping seeding.");
            return;
        }

        Faker faker = new Faker(Locale.forLanguageTag("vi"));
        Set<String> usedEmails = new HashSet<>();
        Set<String> usedUsernames = new HashSet<>();
        List<User> users = new ArrayList<>();

        // Tạo admin user
        users.add(
                User.builder()
                        .username("admin")
                        .fullName("Administrator")
                        .email("admin@example.com")
                        .password("$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6") // password: admin123
                        .enabled(true)
                        .build()
        );
        usedEmails.add("admin@example.com");
        usedUsernames.add("admin");

        // Tạo 20 users ngẫu nhiên
        for (int i = 0; i < 20; i++) {
            String username = faker.name().firstName().toLowerCase()
                    + faker.number().numberBetween(100, 999);
            String email = faker.internet().emailAddress();

            // Đảm bảo username unique
            int suffix = 1;
            while (usedUsernames.contains(username)) {
                username = faker.name().firstName().toLowerCase()
                        + faker.number().numberBetween(100, 999) + suffix++;
            }
            usedUsernames.add(username);

            // Đảm bảo email unique
            suffix = 1;
            while (usedEmails.contains(email)) {
                email = suffix + faker.internet().emailAddress();
                suffix++;
            }
            usedEmails.add(email);

            users.add(
                    User.builder()
                            .username(username)
                            .fullName(faker.name().fullName())
                            .email(email)
                            .password("$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6") // password: admin123
                            .enabled(faker.bool().bool())
                            .build()
            );
        }

        try {
            userRepository.saveAll(users);
            System.out.println(">>> Seeded " + users.size() + " users successfully.");
        } catch (Exception e) {
            System.err.println("Error seeding users: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return 2; // Chạy sau CategorySeeder
    }
}

