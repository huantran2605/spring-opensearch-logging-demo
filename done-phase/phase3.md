
### **PHASE 3: Cấu hình Logging Spring Boot → Logstash**  
**Mục tiêu:** Cấu hình Logback để gửi log JSON qua TCP đến Logstash, tạo Controller mẫu để sinh log với nhiều level (INFO/WARN/ERROR).  
**Thời gian tổng:** ~15 phút

---

#### **Part 3.1: Tạo file logback-spring.xml**
1. Mở Terminal, đảm bảo đang ở folder `spring-boot-app`:

```bash
cd ~/project/spring-opensearch-logging-demo/spring-boot-app
pwd
```

**Kết quả mong đợi:**  
`/Users/huantran/project/spring-opensearch-logging-demo/spring-boot-app`

2. Tạo file `logback-spring.xml` trong thư mục resources:

```bash
cat > src/main/resources/logback-spring.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <!-- Appender gửi log JSON qua TCP đến Logstash -->
    <appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <destination>localhost:5044</destination>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <threadName/>
                <mdc/>
                <arguments/>
                <logstashMarkers/>
                <stackTrace/>
                <keyValuePairs/>   <!-- để thêm custom fields sau -->
            </providers>
        </encoder>
    </appender>

    <!-- Console để debug -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="LOGSTASH"/>
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
EOF
```

3. Kiểm tra file đã được tạo đúng:

```bash
cat src/main/resources/logback-spring.xml
```

**Kết quả mong đợi:**  
- File hiển thị nội dung XML giống hệt ở trên.
- Có 2 appender: `LOGSTASH` (gửi JSON qua TCP port 5044) và `CONSOLE` (in ra terminal).
- Root level là `INFO`.

**Giải thích cấu hình:**
| Thành phần | Mục đích |
|---|---|
| `LogstashTcpSocketAppender` | Gửi log qua TCP đến Logstash (port 5044) |
| `LoggingEventCompositeJsonEncoder` | Encode log thành JSON có cấu trúc |
| `<timestamp/>` | Thời gian log |
| `<logLevel/>` | Level: INFO, WARN, ERROR... |
| `<loggerName/>` | Tên class/logger sinh ra log |
| `<threadName/>` | Thread đang xử lý |
| `<mdc/>` | Mapped Diagnostic Context (dùng cho tracing sau) |
| `<stackTrace/>` | Stack trace khi có exception |
| `<keyValuePairs/>` | Custom fields thêm sau |
| `CONSOLE appender` | In log ra terminal để debug khi dev |

**Reply ngay:** "**Xong 3.1**" khi hoàn tất.

---

#### **Part 3.2: Tạo DemoController – sinh log mẫu**
1. Tạo folder cho Controller (nếu chưa có):

```bash
mkdir -p src/main/java/com/example/demo
```

2. Tạo file `DemoController.java`:

```bash
cat > src/main/java/com/example/demo/DemoController.java << 'EOF'
package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

@RestController
@RequestMapping("/api")
@Slf4j
public class DemoController {

    private final Random random = new Random();

    @GetMapping("/hello")
    public String hello() {
        log.info("Hello API called at {}", LocalDateTime.now());
        return "Hello OpenSearch!";
    }

    @GetMapping("/product/{id}")
    public String getProduct(@PathVariable Long id) {
        log.info("Get product id: {}", id);
        if (random.nextBoolean()) {
            log.warn("Product {} is low stock", id);
        }
        if (random.nextInt(10) == 0) {
            log.error("Database timeout when fetching product {}", id);
            throw new RuntimeException("Demo error");
        }
        return "Product " + id;
    }

    @PostMapping("/order")
    public String createOrder() {
        log.info("Order created successfully");
        return "Order created";
    }
}
EOF
```

3. Kiểm tra file đã tạo đúng:

```bash
cat src/main/java/com/example/demo/DemoController.java
```

**Kết quả mong đợi:**  
File hiển thị đúng nội dung Java ở trên, gồm 3 endpoint:

| Endpoint | Method | Loại log sinh ra |
|---|---|---|
| `/api/hello` | GET | `INFO` – mỗi lần gọi |
| `/api/product/{id}` | GET | `INFO` luôn + `WARN` (50% xác suất) + `ERROR` (10% xác suất, kèm exception) |
| `/api/order` | POST | `INFO` – mỗi lần gọi |

**Giải thích annotation:**
- `@Slf4j` (Lombok): tự động tạo biến `log` để ghi log, không cần khai báo `LoggerFactory.getLogger(...)`.
- `@RestController`: đánh dấu class là REST API controller.
- `@RequestMapping("/api")`: tất cả endpoint trong class bắt đầu bằng `/api`.

**Reply ngay:** "**Xong 3.2**" khi hoàn tất.

---

#### **Part 3.3: Kiểm tra Application class**
1. Kiểm tra file Application class chính (giữ nguyên mặc định, không cần sửa):

```bash
cat src/main/java/com/example/demo/SpringBootAppApplication.java
```

**Kết quả mong đợi:**  
File có nội dung tương tự:

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootAppApplication.class, args);
    }
}
```

> **Lưu ý:** Không cần sửa file này. Annotation `@SpringBootApplication` sẽ tự động scan và load `DemoController` vì cùng package `com.example.demo`.

**Reply ngay:** "**Xong 3.3**" nếu file Application đúng như trên.

---

#### **Part 3.4: Build kiểm tra – đảm bảo code compile**
1. Chạy lệnh build:

```bash
./mvnw clean compile
```

**Kết quả mong đợi:**  
```
[INFO] BUILD SUCCESS
```

- Không có lỗi compile.
- Không có lỗi dependency (logstash-logback-encoder phải resolve được).

2. Kiểm tra lại toàn bộ cấu trúc file đã tạo trong Phase 3:

```bash
find src/main -type f | sort
```

**Kết quả mong đợi:**
```
src/main/java/com/example/demo/DemoController.java
src/main/java/com/example/demo/SpringBootAppApplication.java
src/main/resources/application.properties
src/main/resources/logback-spring.xml
```

Bạn phải thấy **đủ 4 file** trên (2 file Java + 2 file resources).

---

**Phase 3 HOÀN TẤT!** 🎉

**Tổng kết Phase 3 – những gì đã làm:**
- ✅ Tạo `logback-spring.xml` – cấu hình gửi log JSON qua TCP đến Logstash (port 5044)
- ✅ Tạo `DemoController.java` – 3 REST endpoint sinh log INFO/WARN/ERROR
- ✅ Kiểm tra Application class mặc định (không cần sửa)
- ✅ Build thành công, code compile OK

**Reply:** "**Xong Phase 3**" hoặc "**Xong 3.4**"  
Mình sẽ chuyển sang **Phase 4** (Cấu hình Logstash Pipeline).

---
