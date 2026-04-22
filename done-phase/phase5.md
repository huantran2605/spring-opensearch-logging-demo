
### **PHASE 5: Chạy & Test End-to-End**  
**Mục tiêu:** Chạy Spring Boot app, gọi API để sinh log, xác nhận log đi qua Logstash vào OpenSearch thành công.  
**Thời gian tổng:** ~10 phút

> **Lưu ý trước khi bắt đầu:** Đảm bảo 3 Docker container (opensearch, opensearch-dashboards, logstash) đều đang **Up** (đã kiểm tra ở Phase 4).

---

#### **Part 5.1: Build & chạy Spring Boot**
1. Mở Terminal, di chuyển đến folder `spring-boot-app`:

```bash
cd ~/project/spring-opensearch-logging-demo/spring-boot-app
```

2. Build và chạy Spring Boot:

```bash
./mvnw spring-boot:run
```

**Thời gian chờ:** Lần đầu ~30-60 giây (download dependencies + compile).

**Kết quả mong đợi:**  
Trong terminal thấy log tương tự:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

... Started SpringBootAppApplication in X.XXX seconds
```

> **Quan trọng:** Bạn có thể thấy warning `Connection refused` cho Logstash lúc đầu – **KHÔNG SAO**, appender sẽ tự retry. Nếu Logstash đang Up, nó sẽ connect thành công sau vài giây.

**Giữ terminal này mở** (Spring Boot đang chạy). Mở **terminal mới** cho các bước tiếp theo.

**Reply ngay:** "**Xong 5.1**" khi thấy `Started SpringBootAppApplication`.

---

#### **Part 5.2: Gọi API để sinh log**
1. Mở **Terminal mới** (Tab mới hoặc cửa sổ mới).

2. Gọi API `/api/hello` (GET):

```bash
curl http://localhost:8080/api/hello
```

**Kết quả mong đợi:**
```
Hello OpenSearch!
```

3. Gọi API `/api/product/{id}` nhiều lần (GET) – để sinh log INFO + WARN + ERROR:

```bash
# Gọi nhiều lần để có đủ loại log
curl http://localhost:8080/api/product/1
curl http://localhost:8080/api/product/2
curl http://localhost:8080/api/product/3
curl http://localhost:8080/api/product/100
curl http://localhost:8080/api/product/200
curl http://localhost:8080/api/product/999
```

**Kết quả mong đợi:**  
- Phần lớn trả về `Product X` (INFO log).
- Một số lần có thể thấy `Product X is low stock` trong terminal Spring Boot (WARN log).
- Hiếm khi (~10%) gặp lỗi 500 (ERROR log + exception).

4. Gọi API `/api/order` (POST):

```bash
curl -X POST http://localhost:8080/api/order
curl -X POST http://localhost:8080/api/order
curl -X POST http://localhost:8080/api/order
```

**Kết quả mong đợi:**
```
Order created
```

5. (Tùy chọn) Gọi hàng loạt để tạo nhiều log:

```bash
# Tạo 20 requests nhanh
for i in $(seq 1 20); do curl -s http://localhost:8080/api/product/$i > /dev/null; done
echo "Done! 20 requests sent."
```

**Reply ngay:** "**Xong 5.2**" khi đã gọi API thành công.

---

#### **Part 5.3: Kiểm tra log trong terminal Spring Boot**
1. Quay lại **terminal đang chạy Spring Boot** (Part 5.1).

2. Kiểm tra output – bạn sẽ thấy log dạng:

```
23:25:00.123 [http-nio-8080-exec-1] INFO  c.e.demo.DemoController - Hello API called at 2026-04-22T23:25:00.123
23:25:01.456 [http-nio-8080-exec-2] INFO  c.e.demo.DemoController - Get product id: 1
23:25:01.457 [http-nio-8080-exec-2] WARN  c.e.demo.DemoController - Product 1 is low stock
23:25:02.789 [http-nio-8080-exec-3] INFO  c.e.demo.DemoController - Order created successfully
```

**Xác nhận:** Bạn phải thấy log với các level **INFO**, **WARN**, và đôi khi **ERROR** trong terminal.

**Reply ngay:** "**Xong 5.3**" khi thấy log trong terminal Spring Boot.

---

#### **Part 5.4: Kiểm tra log trong Logstash**
1. Mở **terminal mới** (hoặc dùng terminal ở Part 5.2).

2. Xem log của Logstash container:

```bash
cd ~/project/spring-opensearch-logging-demo/docker
docker logs logstash 2>&1 | tail -30
```

**Kết quả mong đợi:**  
Thấy log JSON được in ra (nhờ `stdout { codec => rubydebug }` trong pipeline):

```ruby
{
    "@timestamp" => 2026-04-22T16:25:00.123Z,
      "logLevel" => "INFO",
    "loggerName" => "com.example.demo.DemoController",
    "threadName" => "http-nio-8080-exec-1",
       "message" => "Hello API called at 2026-04-22T23:25:00.123"
}
```

> **Nếu KHÔNG thấy log JSON:**
> - Kiểm tra Logstash có lỗi không: `docker logs logstash 2>&1 | grep -i error`
> - Kiểm tra pipeline đã started: `docker logs logstash 2>&1 | grep "Pipeline main started"`
> - Kiểm tra port 5044: `docker port logstash`

3. (Tùy chọn) Xem log realtime (theo dõi liên tục):

```bash
docker logs -f logstash
```

Nhấn `Ctrl+C` để thoát.

**Reply ngay:** "**Xong 5.4**" khi thấy log JSON trong Logstash.

---

#### **Part 5.5: Kiểm tra data đã vào OpenSearch**
1. Kiểm tra index đã được tạo trong OpenSearch:

```bash
curl -s http://localhost:9200/_cat/indices?v
```

**Kết quả mong đợi:**  
Thấy index dạng `spring-logs-2026.04.22` (ngày hiện tại):

```
health status index                   uuid                   pri rep docs.count docs.deleted store.size pri.store.size
green  open   spring-logs-2026.04.22  xxx                    1   0   25         0            50kb       50kb
```

> **Quan trọng:** `docs.count` phải > 0, nghĩa là log đã vào OpenSearch thành công!

2. Xem mẫu vài log document trong OpenSearch:

```bash
curl -s http://localhost:9200/spring-logs-*/_search?pretty&size=3
```

**Kết quả mong đợi:**  
JSON response chứa log entries, ví dụ:

```json
{
  "hits": {
    "total": { "value": 25, "relation": "eq" },
    "hits": [
      {
        "_source": {
          "@timestamp": "2026-04-22T16:25:00.123Z",
          "logLevel": "INFO",
          "loggerName": "com.example.demo.DemoController",
          "message": "Hello API called at ..."
        }
      }
    ]
  }
}
```

3. (Tùy chọn) Đếm tổng số log:

```bash
curl -s http://localhost:9200/spring-logs-*/_count?pretty
```

**Reply ngay:** "**Xong 5.5**" khi thấy index có data.

---

#### **Part 5.6: Troubleshooting (nếu gặp vấn đề)**

> **Chỉ đọc phần này NẾU gặp lỗi ở các bước trên.**

**Vấn đề 1: Logstash báo lỗi template ECS v8**
```
[ERROR] Failed to install template ... Template file ... ecs-v8/3x.json could not be found
```

**Giải pháp:** Lỗi này là do Logstash plugin chưa hỗ trợ template cho OpenSearch v3. **Log vẫn được gửi bình thường**, chỉ thiếu index template mặc định. Nếu muốn fix:

```bash
# Thêm environment variable vào docker-compose.yml cho logstash service:
# - pipeline.ecs_compatibility=disabled
```

Sau đó restart:
```bash
docker compose restart logstash
```

---

**Vấn đề 2: Spring Boot báo `Connection refused` cho Logstash**

**Giải pháp:** 
- Kiểm tra Logstash đang chạy: `docker compose ps`
- Kiểm tra port: `docker port logstash`
- Logback appender tự retry, đợi 10-30 giây rồi thử lại.

---

**Vấn đề 3: `curl localhost:8080/api/hello` báo `Connection refused`**

**Giải pháp:**
- Đảm bảo Spring Boot đã start xong (thấy `Started SpringBootAppApplication` trong terminal).
- Kiểm tra port 8080 không bị chiếm: `lsof -i :8080`

---

**Vấn đề 4: Không thấy index `spring-logs-*` trong OpenSearch**

**Giải pháp:**
- Kiểm tra Logstash log: `docker logs logstash 2>&1 | grep -i error`
- Kiểm tra OpenSearch hoạt động: `curl http://localhost:9200`
- Thử gọi thêm API và đợi 5-10 giây.

---

**Phase 5 HOÀN TẤT!** 🎉

**Tổng kết Phase 5 – những gì đã làm:**
- ✅ Chạy Spring Boot app thành công
- ✅ Gọi 3 API endpoint để sinh log (INFO/WARN/ERROR)
- ✅ Xác nhận log hiển thị trong terminal Spring Boot (Console appender)
- ✅ Xác nhận log JSON xuất hiện trong Logstash (stdout debug)
- ✅ Xác nhận data đã vào OpenSearch (index `spring-logs-*` có `docs.count > 0`)

**End-to-End flow đã hoạt động:**
```
Spring Boot → TCP:5044 → Logstash → OpenSearch ✅
```

**Reply:** "**Xong Phase 5**" hoặc "**Xong 5.5**"  
Mình sẽ chuyển sang **Phase 6** (Thực hành OpenSearch Dashboards – tạo visualizations & dashboard).

---
