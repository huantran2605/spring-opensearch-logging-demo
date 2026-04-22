
### **PHASE 4: Cấu hình Logstash Pipeline**  
**Mục tiêu:** Kiểm tra và hiểu cấu hình Logstash pipeline đã tạo từ Phase 1, đảm bảo Logstash nhận log JSON từ Spring Boot qua TCP và đẩy vào OpenSearch đúng index.  
**Thời gian tổng:** ~10 phút

> **Lưu ý:** File `docker/logstash/pipeline.conf` đã được tạo sẵn trong Phase 1 khi setup Docker. Phase này tập trung vào **kiểm tra, hiểu cấu hình**, và **đảm bảo Logstash đang hoạt động đúng** trước khi chạy End-to-End ở Phase 5.

---

#### **Part 4.1: Kiểm tra file pipeline.conf đã tồn tại**
1. Mở Terminal, di chuyển đến folder docker:

```bash
cd ~/project/spring-opensearch-logging-demo/docker
pwd
```

**Kết quả mong đợi:**  
`/Users/huantran/project/spring-opensearch-logging-demo/docker`

2. Kiểm tra file pipeline.conf:

```bash
cat logstash/pipeline.conf
```

**Kết quả mong đợi:**  
File hiển thị nội dung sau:

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

**Reply ngay:** "**Xong 4.1**" nếu file tồn tại và nội dung đúng.

---

#### **Part 4.2: Hiểu cấu hình pipeline (quan trọng!)**

Pipeline Logstash gồm 3 phần: **Input → Filter → Output**. Đây là flow dữ liệu:

```
Spring Boot App                Logstash                         OpenSearch
┌──────────────┐    TCP:5044   ┌──────────────────────┐          ┌──────────────┐
│  logback      │──────────────▶│  Input (tcp:5044)    │          │              │
│  JSON encoder │              │     ↓                │          │  Index:       │
│               │              │  Filter (date parse) │          │  spring-logs- │
│  Port 5044    │              │     ↓                │          │  2026.04.22   │
└──────────────┘              │  Output (opensearch)  │─────────▶│              │
                              │  Output (stdout)      │          └──────────────┘
                              └──────────────────────┘
                                    ↓
                              Terminal (debug log)
```

**Giải thích chi tiết từng phần:**

| Phần | Cấu hình | Giải thích |
|---|---|---|
| **Input** | `tcp { port => 5044, codec => json_lines }` | Lắng nghe TCP port 5044, parse mỗi dòng nhận được như JSON |
| **Filter** | `date { match => ["@timestamp", "ISO8601"] }` | Parse trường `@timestamp` theo chuẩn ISO8601 để OpenSearch hiểu được thời gian |
| **Output 1** | `opensearch { hosts => [...], index => "spring-logs-%{+YYYY.MM.dd}" }` | Gửi log vào OpenSearch, tạo index theo ngày (ví dụ: `spring-logs-2026.04.22`) |
| **Output 2** | `stdout { codec => rubydebug }` | In log ra terminal Logstash để debug |

**Tại sao `codec => json_lines`?**  
- Spring Boot (qua `LogstashTcpSocketAppender`) gửi mỗi dòng log là 1 JSON object, kết thúc bằng newline.
- `json_lines` tự động parse mỗi dòng thành JSON event.

**Tại sao index theo ngày `spring-logs-%{+YYYY.MM.dd}`?**  
- Dễ quản lý: mỗi ngày 1 index riêng
- Dễ xóa data cũ: chỉ cần xóa index của ngày đó
- Thực tế production cũng dùng pattern này (kết hợp ILM policy)

**Reply ngay:** "**Xong 4.2**" khi đã đọc hiểu.

---

#### **Part 4.3: Kiểm tra Logstash container đang chạy**
1. Kiểm tra trạng thái 3 container:

```bash
docker compose ps
```

**Kết quả mong đợi:**  
3 container đều **Up**:
- `opensearch` – Up (healthy)
- `opensearch-dashboards` – Up
- `logstash` – Up

2. Kiểm tra Logstash log để đảm bảo pipeline đã load:

```bash
docker logs logstash 2>&1 | tail -20
```

**Kết quả mong đợi:**  
Bạn sẽ thấy log tương tự:
```
[INFO ] ... Pipeline main started
[INFO ] ... Successfully started Logstash API endpoint {:port=>9600}
```

> **Quan trọng:** Nếu thấy dòng `Pipeline main started` → Logstash đã load pipeline.conf thành công và đang lắng nghe TCP port 5044.

3. (Tùy chọn) Kiểm tra port 5044 đang mở:

```bash
docker exec logstash curl -s localhost:9600/_node/stats | grep -o '"events":{[^}]*}'
```

**Reply ngay:** "**Xong 4.3**" khi Logstash đang Up và pipeline đã started.

---

#### **Part 4.4: Restart Logstash (nếu cần)**

> **Chỉ làm bước này NẾU** Logstash có lỗi hoặc bạn đã thay đổi file `pipeline.conf`.

Nếu cần restart Logstash để load lại config:

```bash
docker compose restart logstash
```

Sau đó kiểm tra lại:

```bash
docker logs -f logstash 2>&1 | tail -30
```

**Kết quả mong đợi:**  
- Thấy `Pipeline main started` trong log.
- Không có dòng `ERROR`.

Nhấn `Ctrl+C` để thoát xem log.

**Reply ngay:** "**Xong 4.4**" hoặc bỏ qua nếu không cần restart.

---

**Phase 4 HOÀN TẤT!** 🎉

**Tổng kết Phase 4 – những gì đã làm:**
- ✅ Kiểm tra file `pipeline.conf` tồn tại và nội dung đúng
- ✅ Hiểu flow: Spring Boot → TCP:5044 → Logstash → OpenSearch (index theo ngày)
- ✅ Xác nhận Logstash container đang Up và pipeline đã started
- ✅ Biết cách restart Logstash nếu cần

**Reply:** "**Xong Phase 4**" hoặc "**Xong 4.4**"  
Mình sẽ chuyển sang **Phase 5** (Chạy & Test End-to-End).

---
