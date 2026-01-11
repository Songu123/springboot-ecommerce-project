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
//        System.out.println("Seeders: " + seeders);
//
//        seeders.stream()
//                .sorted(Comparator.comparingInt(BaseSeeder::order))
//                .forEach(BaseSeeder::seed);
        CategorySeeder categorySeeder = new CategorySeeder();
    }
}
