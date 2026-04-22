
---

### **PHASE 1: Setup OpenSearch + Dashboards + Logstash (Docker)**  
**Mục tiêu:** Chạy 3 service ổn định trên Mac Apple Silicon (M1/M2/M3/M4), tắt security để demo local dễ dùng.  
**Thời gian tổng:** ~15 phút

#### **Part 1.1: Tạo folder project và cấu trúc**
1. Mở **Terminal** trên Mac.
2. Chạy lần lượt các lệnh sau:

```bash
# Tạo folder chính
mkdir -p ~/project/spring-opensearch-logging-demo/docker/logstash
cd ~/project/spring-opensearch-logging-demo

# Kiểm tra folder đã có
ls -la
```

**Kết quả mong đợi:**  
Bạn thấy folder `docker` và bên trong có folder `logstash`.

**Reply ngay:** “**Xong 1.1**” khi hoàn tất.

---

#### **Part 1.2: Tạo file docker-compose.yml**
1. Vẫn ở Terminal, tạo file bằng lệnh:

```bash
cat > docker/docker-compose.yml << 'EOF'
version: '3.8'

services:
  opensearch:
    image: opensearchproject/opensearch:latest          # tự động arm64 trên Apple Silicon
    container_name: opensearch
    environment:
      - discovery.type=single-node
      - plugins.security.disabled=true                 # tắt security cho local demo
      - OPENSEARCH_JAVA_OPTS=-Xms512m -Xmx512m
      - OPENSEARCH_INITIAL_ADMIN_PASSWORD=MyComplexPassword123!@# # Required for latest image setup
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
      - DISABLE_SECURITY_DASHBOARDS_PLUGIN=true
    depends_on:
      - opensearch

  logstash:
    image: opensearchproject/logstash-oss-with-opensearch-output-plugin:latest
    container_name: logstash
    volumes:
      - ./logstash/pipeline.conf:/usr/share/logstash/pipeline/pipeline.conf:ro
    ports:
      - "5044:5044"
    depends_on:
      - opensearch

volumes:
  opensearch_data:
EOF
```

2. Kiểm tra file đã tạo đúng:

```bash
cat docker/docker-compose.yml
```

**Kết quả mong đợi:** Nội dung file giống hệt đoạn trên.

**Reply ngay:** “**Xong 1.2**” khi hoàn tất.

---

#### **Part 1.3: Chạy Docker Compose**
1. Di chuyển vào folder docker:

```bash
cd docker
```

2. Khởi động 3 service:

```bash
docker compose up -d
```

**Thời gian chờ:** 20-40 giây (lần đầu sẽ tải image ~800MB).

**Reply ngay:** “**Xong 1.3**” sau khi lệnh chạy xong (không lỗi).

---

#### **Part 1.4: Kiểm tra thành công**
Chạy các lệnh kiểm tra sau (vẫn ở folder `docker`):

```bash
# 1. Kiểm tra 3 container đang chạy
docker compose ps

# 2. Kiểm tra OpenSearch hoạt động
curl -s http://localhost:9200 | grep "opensearch"

# 3. Kiểm tra OpenSearch Dashboards (mở browser)
open http://localhost:5601
```

**Kết quả mong đợi:**
- `docker compose ps` → 3 container: `opensearch`, `opensearch-dashboards`, `logstash` đều **Up** (State = Up).
- curl trả về JSON có `"name": "opensearch"` hoặc tương tự.
- Browser mở trang OpenSearch Dashboards (màn hình Welcome, không yêu cầu login).

Nếu tất cả OK → **Phase 1 HOÀN TẤT!**

**Reply:** “**Xong Phase 1**” hoặc “**Xong 1.4**”  
Mình sẽ chuyển sang **Phase 2** ngay (Tạo Spring Boot Project).

---
