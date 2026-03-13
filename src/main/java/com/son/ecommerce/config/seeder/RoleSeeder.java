package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.Permission;
import com.son.ecommerce.entity.Role;
import com.son.ecommerce.repository.PermissionRepository;
import com.son.ecommerce.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class RoleSeeder implements BaseSeeder {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void seed() {
        System.out.println(">>> RoleSeeder is running...");

        if (roleRepository.count() > 0) {
            System.out.println(">>> Roles already exist. Skipping seeding.");
            return;
        }

        // Get all permissions
        List<Permission> allPermissions = permissionRepository.findAll();

        // Create roles with specific permissions
        List<Role> roles = new ArrayList<>();

        // ADMIN - has all permissions
        Role adminRole = Role.builder()
                .name("ROLE_ADMIN")
                .description("Administrator with full access")
                .permissions(new HashSet<>(allPermissions))
                .build();
        roles.add(adminRole);

        // MANAGER - can manage products, categories, and orders
        Set<Permission> managerPerms = new HashSet<>();
        managerPerms.addAll(permissionRepository.findByCategory("PRODUCT"));
        managerPerms.addAll(permissionRepository.findByCategory("CATEGORY"));
        managerPerms.addAll(permissionRepository.findByCategory("ORDER"));
        permissionRepository.findByName("VIEW_DASHBOARD").ifPresent(managerPerms::add);
        permissionRepository.findByName("VIEW_REPORTS").ifPresent(managerPerms::add);

        Role managerRole = Role.builder()
                .name("ROLE_MANAGER")
                .description("Manager with limited admin access")
                .permissions(managerPerms)
                .build();
        roles.add(managerRole);

        // USER - can view products and manage own orders
        Set<Permission> userPerms = new HashSet<>();
        permissionRepository.findByName("READ_PRODUCT").ifPresent(userPerms::add);
        permissionRepository.findByName("READ_CATEGORY").ifPresent(userPerms::add);
        permissionRepository.findByName("CREATE_ORDER").ifPresent(userPerms::add);
        permissionRepository.findByName("READ_ORDER").ifPresent(userPerms::add);
        permissionRepository.findByName("UPDATE_ORDER").ifPresent(userPerms::add);

        Role userRole = Role.builder()
                .name("ROLE_USER")
                .description("Regular user")
                .permissions(userPerms)
                .build();
        roles.add(userRole);

        // CUSTOMER - similar to USER but with different name
        Role customerRole = Role.builder()
                .name("ROLE_CUSTOMER")
                .description("Customer with shopping access")
                .permissions(userPerms)
                .build();
        roles.add(customerRole);

        try {
            roleRepository.saveAll(roles);
            System.out.println(">>> Seeded " + roles.size() + " roles successfully.");
            System.out.println("    - ROLE_ADMIN: " + adminRole.getPermissions().size() + " permissions");
            System.out.println("    - ROLE_MANAGER: " + managerRole.getPermissions().size() + " permissions");
            System.out.println("    - ROLE_USER: " + userRole.getPermissions().size() + " permissions");
            System.out.println("    - ROLE_CUSTOMER: " + customerRole.getPermissions().size() + " permissions");
        } catch (Exception e) {
            System.err.println("Error seeding roles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return 0; // Chạy sau PermissionSeeder
    }
}

