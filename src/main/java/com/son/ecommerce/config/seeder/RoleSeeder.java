package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.Role;
import com.son.ecommerce.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class RoleSeeder implements BaseSeeder {
    private final RoleRepository roleRepository;

    @Override
    public void seed() {
        System.out.println(">>> RoleSeeder is running...");

        if (roleRepository.count() > 0) {
            System.out.println(">>> Roles already exist. Skipping seeding.");
            return;
        }

        List<Role> roles = new ArrayList<>();

        // Tạo các roles cơ bản
        roles.add(Role.builder().name("ROLE_ADMIN").build());
        roles.add(Role.builder().name("ROLE_USER").build());
        roles.add(Role.builder().name("ROLE_MANAGER").build());
        roles.add(Role.builder().name("ROLE_CUSTOMER").build());

        try {
            roleRepository.saveAll(roles);
            System.out.println(">>> Seeded " + roles.size() + " roles successfully.");
        } catch (Exception e) {
            System.err.println("Error seeding roles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return 0; // Chạy đầu tiên
    }
}

