package com.son.ecommerce.config.seeder;

import com.son.ecommerce.entity.Order;
import com.son.ecommerce.entity.OrderItem;
import com.son.ecommerce.entity.Product;
import com.son.ecommerce.entity.User;
import com.son.ecommerce.repository.OrderRepository;
import com.son.ecommerce.repository.ProductRepository;
import com.son.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class OrderSeeder implements BaseSeeder {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    public void seed() {
        System.out.println(">>> OrderSeeder is running...");

        if (orderRepository.count() > 0) {
            System.out.println(">>> Orders already exist. Skipping seeding.");
            return;
        }

        // Lấy tất cả users và products
        List<User> users = userRepository.findAll();
        List<Product> products = productRepository.findAll();

        if (users.isEmpty()) {
            System.err.println(">>> No users found. Please run UserSeeder first.");
            return;
        }

        if (products.isEmpty()) {
            System.err.println(">>> No products found. Please run ProductSeeder first.");
            return;
        }

        Faker faker = new Faker(Locale.forLanguageTag("vi"));
        Random random = new Random();
        List<Order> orders = new ArrayList<>();

        String[] statuses = {"NEW", "CONFIRMED", "PAID", "CANCELLED"};

        // Tạo 30 orders ngẫu nhiên
        for (int i = 0; i < 30; i++) {
            // Random user
            User randomUser = users.get(random.nextInt(users.size()));

            // Random status
            String status = statuses[random.nextInt(statuses.length)];

            // Random created date (trong vòng 60 ngày gần đây)
            LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(60));

            Order order = new Order();
            order.setUser(randomUser);
            order.setStatus(status);
            order.setCreatedAt(createdAt);

            // Tạo 1-5 order items cho mỗi order
            List<OrderItem> orderItems = new ArrayList<>();
            int numberOfItems = random.nextInt(5) + 1;
            double totalPrice = 0;

            for (int j = 0; j < numberOfItems; j++) {
                // Random product
                Product randomProduct = products.get(random.nextInt(products.size()));
                int quantity = random.nextInt(5) + 1;
                double price = randomProduct.getPrice();

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(randomProduct);
                orderItem.setQuantity(quantity);
                orderItem.setPrice(price);
                orderItem.setOrder(order);

                orderItems.add(orderItem);
                totalPrice += price * quantity;
            }

            order.setItems(orderItems);
            order.setTotalPrice(totalPrice);

            orders.add(order);
        }

        try {
            orderRepository.saveAll(orders);
            System.out.println(">>> Seeded " + orders.size() + " orders successfully.");
        } catch (Exception e) {
            System.err.println("Error seeding orders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int order() {
        return 4; // Chạy sau ProductSeeder và UserSeeder
    }
}

