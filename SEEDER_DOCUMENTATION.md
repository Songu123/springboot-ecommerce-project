# Database Seeder Documentation

## Tổng quan

Hệ thống seeder tự động tạo dữ liệu mẫu cho database khi chạy ứng dụng ở môi trường `dev`.

## Các Seeder có sẵn

### 1. RoleSeeder (Order: 0)
- **Mục đích**: Tạo các roles cơ bản cho hệ thống
- **Dữ liệu tạo**:
  - ROLE_ADMIN
  - ROLE_USER
  - ROLE_MANAGER
  - ROLE_CUSTOMER

### 2. CategorySeeder (Order: 1)
- **Mục đích**: Tạo các danh mục sản phẩm
- **Số lượng**: 24 categories
- **Dữ liệu mẫu**:
  - Điện thoại & Phụ kiện
  - Laptop & Máy tính bảng
  - Máy ảnh & Quay phim
  - Thời trang Nam/Nữ
  - Mỹ phẩm
  - Và nhiều danh mục khác...

### 3. UserSeeder (Order: 2)
- **Mục đích**: Tạo users với các roles khác nhau
- **Số lượng**: 23 users (3 default + 20 random)
- **Users mặc định**:
  - **admin** / admin123 (ROLE_ADMIN)
  - **manager** / manager123 (ROLE_MANAGER)
  - **customer** / customer123 (ROLE_CUSTOMER)
- **20 random users** với password: **password123**

### 4. ProductSeeder (Order: 3)
- **Mục đích**: Tạo sản phẩm cho từng danh mục
- **Số lượng**: 100 products
- **Đặc điểm**:
  - Sản phẩm phù hợp với từng danh mục
  - Giá cả realistic theo loại sản phẩm
  - Số lượng tồn kho ngẫu nhiên (1-100)
  - Mô tả ngẫu nhiên

### 5. OrderSeeder (Order: 4)
- **Mục đích**: Tạo đơn hàng mẫu
- **Số lượng**: 30 orders
- **Đặc điểm**:
  - Mỗi order có 1-5 order items
  - Status ngẫu nhiên: NEW, CONFIRMED, PAID, CANCELLED
  - Ngày tạo trong vòng 60 ngày gần đây
  - Tính toán tổng giá tự động

## Thứ tự chạy Seeder

```
0. RoleSeeder
1. CategorySeeder
2. UserSeeder (cần Roles và Categories)
3. ProductSeeder (cần Categories)
4. OrderSeeder (cần Users và Products)
```

## Cách sử dụng

### 1. Chạy tự động khi start ứng dụng

Seeders sẽ tự động chạy khi:
- Profile active là `dev`
- Database chưa có dữ liệu tương ứng

```yaml
# application.yaml
spring:
  profiles:
    active: dev
```

### 2. Chạy ứng dụng

```bash
# Windows
.\gradlew bootRun

# Linux/Mac
./gradlew bootRun
```

### 3. Kiểm tra log

Khi seeders chạy, bạn sẽ thấy log như sau:

```
========================================
   DATABASE SEEDING STARTED
========================================

>>> RoleSeeder is running...
>>> Seeded 4 roles successfully.

>>> CategorySeeder is running...
>>> Seeded 24 categories successfully.

>>> UserSeeder is running...
>>> Seeded 23 users successfully.
>>> Default users:
    - admin/admin123 (ROLE_ADMIN)
    - manager/manager123 (ROLE_MANAGER)
    - customer/customer123 (ROLE_CUSTOMER)

>>> ProductSeeder is running...
>>> Seeded 100 products successfully.

>>> OrderSeeder is running...
>>> Seeded 30 orders successfully.

========================================
   DATABASE SEEDING COMPLETED
========================================
```

## Reset Database

Để chạy lại seeders từ đầu:

### Cách 1: Xóa database và tạo lại

```sql
DROP DATABASE ecommerce_db_new;
CREATE DATABASE ecommerce_db_new;
```

### Cách 2: Thay đổi ddl-auto

```yaml
# application.yaml
spring:
  jpa:
    hibernate:
      ddl-auto: create  # Hoặc create-drop
```

⚠️ **Lưu ý**: `create` sẽ xóa toàn bộ dữ liệu hiện có!

## Tùy chỉnh Seeders

### Thay đổi số lượng dữ liệu

Mở file seeder tương ứng và thay đổi số lượng:

```java
// ProductSeeder.java
for (int i = 0; i < 100; i++) { // Đổi 100 thành số khác
    // ...
}
```

### Thêm Seeder mới

1. Tạo class implement `BaseSeeder`:

```java
@Component
@RequiredArgsConstructor
@Profile("dev")
public class CustomSeeder implements BaseSeeder {
    
    @Override
    public void seed() {
        // Logic tạo dữ liệu
    }
    
    @Override
    public int order() {
        return 5; // Thứ tự chạy
    }
}
```

2. Seeder sẽ tự động được DatabaseSeeder phát hiện và chạy

## Tắt Seeding

### Cách 1: Đổi profile

```yaml
# application.yaml
spring:
  profiles:
    active: prod  # Hoặc bất kỳ profile nào khác ngoài dev
```

### Cách 2: Comment @Profile

```java
// @Profile("dev")  // Comment dòng này
public class CategorySeeder implements BaseSeeder {
    // ...
}
```

## Xử lý lỗi

### Lỗi: "No categories found"
- **Nguyên nhân**: CategorySeeder chưa chạy
- **Giải pháp**: Đảm bảo order() của CategorySeeder < ProductSeeder

### Lỗi: "No users found"  
- **Nguyên nhân**: UserSeeder chưa chạy
- **Giải pháp**: Đảm bảo order() của UserSeeder < OrderSeeder

### Lỗi: Duplicate key
- **Nguyên nhân**: Dữ liệu đã tồn tại
- **Giải pháp**: Seeders tự động skip nếu dữ liệu đã có. Nếu muốn reset, xóa database

## Best Practices

1. ✅ Luôn kiểm tra dữ liệu đã tồn tại trước khi seed
2. ✅ Sử dụng `@Profile("dev")` để tránh chạy ở production
3. ✅ Log rõ ràng để dễ debug
4. ✅ Xử lý exceptions properly
5. ✅ Đặt order() hợp lý để đảm bảo dependencies
6. ✅ Tạo dữ liệu realistic cho testing tốt hơn

## Dependencies

Seeders sử dụng các thư viện:

- **Datafaker**: Tạo dữ liệu giả ngẫu nhiên
- **Lombok**: Giảm boilerplate code
- **Spring Boot**: CommandLineRunner để tự động chạy

```gradle
// build.gradle
implementation 'net.datafaker:datafaker:2.0.2'
compileOnly 'org.projectlombok:lombok'
```

## Liên hệ

Nếu có vấn đề với seeders, vui lòng kiểm tra:
1. Log console khi start ứng dụng
2. Database có tạo thành công không
3. Profile có đúng là `dev` không
4. Dependencies đã được cài đặt chưa

