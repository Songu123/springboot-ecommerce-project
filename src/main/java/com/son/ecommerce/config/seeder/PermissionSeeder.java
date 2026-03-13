package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.Permission;
import com.son.ecommerce.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class PermissionSeeder implements BaseSeeder {
    private final PermissionRepository permissionRepository;

    @Override
    public void seed() {
        System.out.println(">>> PermissionSeeder is running...");

        if (permissionRepository.count() > 0) {
            System.out.println(">>> Permissions already exist. Skipping seeding.");
            return;
        }

        List<Permission> permissions = new ArrayList<>();

        // USER Permissions
        permissions.add(Permission.builder()
                .name("READ_USER")
                .description("View user details")
                .category("USER")
                .build());
        permissions.add(Permission.builder()
                .name("CREATE_USER")
                .description("Create new user")
                .category("USER")
                .build());
        permissions.add(Permission.builder()
                .name("UPDATE_USER")
                .description("Update user information")
                .category("USER")
                .build());
        permissions.add(Permission.builder()
                .name("DELETE_USER")
                .description("Delete user")
                .category("USER")
                .build());

        // PRODUCT Permissions
        permissions.add(Permission.builder()
                .name("READ_PRODUCT")
                .description("View product details")
                .category("PRODUCT")
                .build());
        permissions.add(Permission.builder()
                .name("CREATE_PRODUCT")
                .description("Create new product")
                .category("PRODUCT")
                .build());
        permissions.add(Permission.builder()
                .name("UPDATE_PRODUCT")
                .description("Update product information")
                .category("PRODUCT")
                .build());
        permissions.add(Permission.builder()
                .name("DELETE_PRODUCT")
                .description("Delete product")
                .category("PRODUCT")
                .build());

        // CATEGORY Permissions
        permissions.add(Permission.builder()
                .name("READ_CATEGORY")
                .description("View category details")
                .category("CATEGORY")
                .build());
        permissions.add(Permission.builder()
                .name("CREATE_CATEGORY")
                .description("Create new category")
                .category("CATEGORY")
                .build());
        permissions.add(Permission.builder()
                .name("UPDATE_CATEGORY")
                .description("Update category information")
                .category("CATEGORY")
                .build());
        permissions.add(Permission.builder()
                .name("DELETE_CATEGORY")
                .description("Delete category")
                .category("CATEGORY")
                .build());

        // ORDER Permissions
        permissions.add(Permission.builder()
                .name("READ_ORDER")
                .description("View order details")
                .category("ORDER")
                .build());
        permissions.add(Permission.builder()
                .name("CREATE_ORDER")
                .description("Create new order")
                .category("ORDER")
                .build());
        permissions.add(Permission.builder()
                .name("UPDATE_ORDER")
                .description("Update order status")
                .category("ORDER")
                .build());
        permissions.add(Permission.builder()
                .name("DELETE_ORDER")
                .description("Cancel/Delete order")
                .category("ORDER")
                .build());

        // ROLE & PERMISSION Management
        permissions.add(Permission.builder()
                .name("MANAGE_ROLES")
                .description("Manage roles and permissions")
                .category("ADMIN")
                .build());
        permissions.add(Permission.builder()
                .name("MANAGE_PERMISSIONS")
                .description("Manage system permissions")
                .category("ADMIN")
                .build());

        // DASHBOARD
        permissions.add(Permission.builder()
                .name("VIEW_DASHBOARD")
                .description("Access admin dashboard")
                .category("DASHBOARD")
                .build());
        permissions.add(Permission.builder()
                .name("VIEW_REPORTS")
                .description("View reports and analytics")
                .category("DASHBOARD")
                .build());

        try {
            permissionRepository.saveAll(permissions);
            System.out.println(">>> Seeded " + permissions.size() + " permissions successfully.");
        } catch (Exception e) {
            System.err.println("Error seeding permissions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return -1; // Chạy đầu tiên (trước RoleSeeder)
    }
}

