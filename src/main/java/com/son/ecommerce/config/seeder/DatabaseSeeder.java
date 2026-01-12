package com.son.ecommerce.config.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {

    private final List<BaseSeeder> seeders;

    @Override
    public void run(String... args) {
        System.out.println("\n========================================");
        System.out.println("   DATABASE SEEDING STARTED");
        System.out.println("========================================\n");

        seeders.stream()
                .sorted(Comparator.comparingInt(BaseSeeder::order))
                .forEach(seeder -> {
                    try {
                        seeder.seed();
                    } catch (Exception e) {
                        System.err.println("Error running seeder: " + seeder.getClass().getSimpleName());
                        e.printStackTrace();
                    }
                });

        System.out.println("\n========================================");
        System.out.println("   DATABASE SEEDING COMPLETED");
        System.out.println("========================================\n");
    }
}
