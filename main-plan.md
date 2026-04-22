thực hành với opensearch cho backend web developer
---

### **PHASE 1: Setup OpenSearch + Dashboards + Logstash (Docker) – 15 phút**
**Mục tiêu:** Chạy 3 service ổn định trên Apple Silicon, tắt security để demo local dễ dùng.

Tạo file: `docker/docker-compose.yml`

```yaml
version: '3.8'

services:
  opensearch:
    image: opensearchproject/opensearch:latest          # tự động dùng arm64 trên M-series
    container_name: opensearch
    environment:
      - discovery.type=single-node
      - plugins.security.disabled=true                 # tắt security cho local
      - OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m
    ports:
      - "9200:9200"
    volumes:
      - opensearch_data:/usr/share/opensearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200 || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5

  opensearch-dashboards:
    image: opensearchproject/opensearch-dashboards:latest
    container_name: opensearch-dashboards
    ports:
      - "5601:5601"
    environment:
      - OPENSEARCH_HOSTS=["http://opensearch:9200"]
    depends_on:
      - opensearch

  logstash:
    image: opensearchproject/logstash-oss-with-opensearch-output-plugin:latest
    container_name: logstash
    volumes:
      - ./logstash/pipeline.conf:/usr/share/logstash/pipeline/pipeline.conf:ro
    ports:
      - "5044:5044"     # Spring Boot sẽ gửi log qua TCP 5044
    environment:
      - xpack.monitoring.enabled=false
    depends_on:
      - opensearch

volumes:
  opensearch_data:
```

**Chạy:**
```bash
cd docker
docker compose up -d
```

**Kiểm tra:**
- `docker compose ps` → 3 container phải Up
- Truy cập: http://localhost:9200 (phải thấy JSON OpenSearch)
- http://localhost:5601 (Dashboards mở ra được, không cần user/pass)

---

### **PHASE 2: Tạo Spring Boot Project – 10 phút**
Vào folder `spring-boot-app`

Dùng Spring Initializr (hoặc IDE) với:
- Project: Maven
- Language: Java
- Spring Boot: 3.3.x (hoặc mới nhất)
- Java: 21
- Dependencies: **Spring Web**, **Lombok** (optional)

**pom.xml** (thêm dependency Logstash encoder):

```xml
<dependencies>
    <!-- Spring Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Logstash JSON encoder -->
    <dependency>
        <groupId>net.logstash.logback</groupId>
        <artifactId>logstash-logback-encoder</artifactId>
        <version>7.4</version>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

### **PHASE 3: Cấu hình Logging Spring Boot → Logstash – 15 phút**

1. Tạo file: `spring-boot-app/src/main/resources/logback-spring.xml`

```xml
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
```

2. Tạo Controller mẫu để sinh log (file: `spring-boot-app/src/main/java/com/example/demo/DemoController.java`)

```java
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
```

3. Application class giữ nguyên mặc định.

---

### **PHASE 4: Cấu hình Logstash Pipeline – 10 phút**

Tạo file: `docker/logstash/pipeline.conf`

```ruby
input {
  tcp {
    port => 5044
    codec => json_lines   # nhận log JSON từ Spring Boot
  }
}

filter {
  # Thêm timestamp nếu chưa có
  date {
    match => ["@timestamp", "ISO8601"]
  }
  # Có thể thêm custom fields sau
}

output {
  opensearch {
    hosts => ["http://opensearch:9200"]
    index => "spring-logs-%{+YYYY.MM.dd}"   # index theo ngày
    user => ""      # vì đã tắt security
    password => ""
  }
  # stdout để debug Logstash
  stdout { codec => rubydebug }
}
```

---

### **PHASE 5: Chạy & Test End-to-End – 10 phút**

1. Build & chạy Spring Boot:
   ```bash
   cd spring-boot-app
   ./mvnw spring-boot:run
   ```

2. Mở browser:
   - Gọi các API nhiều lần:
     ```
     http://localhost:8080/api/hello
     http://localhost:8080/api/product/123
     http://localhost:8080/api/order   (POST)
     ```

3. Kiểm tra Logstash log:
   ```bash
   docker logs -f logstash
   ```
   → Phải thấy log JSON được in ra.

---

### **PHASE 6: Thực hành OpenSearch Dashboards (Mức Trung Bình) – 30-45 phút**

1. Vào http://localhost:5601  
2. **Stack Management → Index Patterns**  
   - Create index pattern: `spring-logs-*`  
   - Time field: `@timestamp`  
3. **Discover** → xem logs realtime, filter, search (ví dụ: `level:ERROR` hoặc `message:"product"`)  
4. **Dashboard** → tạo dashboard mẫu:
   - Visualization 1: Area chart – số request theo thời gian
   - Visualization 2: Pie chart – phân bố level (INFO/WARN/ERROR)
   - Visualization 3: Data table – top 10 logger
   - Visualization 4: Metric – tổng error count

---

### **PHASE 7: Hoàn tất & Next Steps (tùy chọn)**
- Tắt Docker khi xong: `docker compose down`
- Cleanup volume nếu muốn reset: `docker volume rm docker_opensearch_data`
- Next level (nếu bạn muốn sau): ingest pipeline, ILM policy, vector search, alerting…

---
