
### **PHASE 2: Tạo Spring Boot Project**  
**Mục tiêu:** Tạo project Spring Boot 3.3.5 + Java 21 + Maven, đã có sẵn Spring Web + Lombok, và chuẩn bị thêm Logstash encoder.  
**Thời gian tổng:** ~10 phút

#### **Part 2.1: Quay về folder gốc project**
1. Mở Terminal (nếu chưa mở).
2. Chạy lệnh sau để quay về đúng thư mục gốc:

```bash
cd ~/project/spring-opensearch-logging-demo
```

3. Kiểm tra bạn đang ở đúng chỗ:

```bash
pwd
ls -la
```

**Kết quả mong đợi:**  
Bạn thấy folder `docker` và `spring-boot-app` (chưa có) trong danh sách.

**Reply ngay:** “**Xong 2.1**” khi hoàn tất.

---

#### **Part 2.2: Tạo project Spring Boot bằng Spring Initializr (qua Terminal)**
Vẫn ở folder gốc, chạy **1 lệnh duy nhất** sau (copy-paste toàn bộ):

```bash
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.3.5 \
  -d baseDir=spring-boot-app \
  -d groupId=com.example.demo \
  -d artifactId=spring-boot-app \
  -d name=spring-boot-app \
  -d description=Demo%20OpenSearch%20Logging \
  -d javaVersion=21 \
  -d packaging=jar \
  -d dependencies=web,lombok \
  -o spring-boot-app.zip && \
unzip -q spring-boot-app.zip && \
rm spring-boot-app.zip
```

**Sau khi chạy xong:**

```bash
ls
```

**Kết quả mong đợi:**  
Thấy folder `spring-boot-app` mới xuất hiện (bên cạnh folder `docker`).

**Reply ngay:** “**Xong 2.2**” khi lệnh chạy xong không lỗi.

---

#### **Part 2.3: Thêm dependency Logstash encoder vào pom.xml**
1. Mở file pom.xml:

```bash
cd spring-boot-app
code pom.xml          # nếu dùng VS Code
# hoặc
open pom.xml          # mở bằng TextEdit / IntelliJ
```

2. Trong thẻ `<dependencies>`, **thêm khối sau** ngay trước thẻ `</dependencies>` (copy-paste nguyên khối):

```xml
    <!-- Logstash JSON encoder - gửi log có cấu trúc đến Logstash -->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
        <version>7.4</version>
    </dependency>
```

**Ví dụ vị trí sau khi thêm (không cần copy phần này):**
```xml
    <dependencies>
        <!-- các dependency mặc định của Spring Initializr -->
        ...
        <!-- Logstash JSON encoder - gửi log có cấu trúc đến Logstash -->
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>7.4</version>
        </dependency>
    </dependencies>
```

3. Lưu file.

**Reply ngay:** “**Xong 2.3**” khi đã thêm xong.

---

#### **Part 2.4: Kiểm tra project Spring Boot**
Chạy lệnh sau để kiểm tra project build được:

```bash
./mvnw clean compile
```

**Kết quả mong đợi:**  
- BUILD SUCCESS  
- Không có lỗi nào liên quan đến dependency.

Sau đó kiểm tra cấu trúc:

```bash
ls
```

Bạn sẽ thấy: `pom.xml`, `src/`, `mvnw`, v.v.

**Phase 2 HOÀN TẤT!**

---