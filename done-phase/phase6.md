
### **PHASE 6: Thực hành OpenSearch Dashboards (Mức Trung Bình)**  
**Mục tiêu:** Sử dụng OpenSearch Dashboards để tạo Index Pattern, khám phá log qua Discover, và xây dựng Dashboard với 4 visualizations.  
**Thời gian tổng:** ~30-45 phút

> **Yêu cầu trước khi bắt đầu:**
> - Spring Boot app đang chạy (Phase 5)
> - 3 Docker container đều Up
> - Đã có data trong OpenSearch (index `spring-logs-*`)

---

#### **Part 6.1: Mở OpenSearch Dashboards & Tạo Index Pattern**

1. Mở browser, truy cập:

```
http://localhost:5601
```

**Kết quả mong đợi:** Trang OpenSearch Dashboards mở ra (không yêu cầu login vì đã tắt security).

2. Nếu thấy trang **Welcome**, chọn **"Explore on my own"** hoặc **"Add data"** → bỏ qua.

3. Tạo Index Pattern:
   - Click menu ☰ (hamburger) ở góc trái trên
   - Chọn **Stack Management** (hoặc **Management**) ở cuối menu
   - Trong trang Management, click **Index Patterns** (bên trái)
   - Click nút **"Create index pattern"**

4. Điền thông tin:

| Trường | Giá trị |
|---|---|
| **Index pattern name** | `spring-logs-*` |

   - Sau khi gõ `spring-logs-*`, hệ thống phải hiển thị **"Your index pattern matches X source(s)"** (X > 0).
   - Click **"Next step"**

5. Chọn Time field:

| Trường | Giá trị |
|---|---|
| **Time field** | `@timestamp` |

   - Chọn `@timestamp` từ dropdown
   - Click **"Create index pattern"**

**Kết quả mong đợi:**  
- Index pattern `spring-logs-*` được tạo thành công.
- Bạn thấy danh sách các fields: `@timestamp`, `level`, `logger_name`, `thread_name`, `message`, `stack_trace`, v.v.

**Reply ngay:** "**Xong 6.1**" khi tạo index pattern thành công.

---

#### **Part 6.2: Khám phá log với Discover**

1. Mở Discover:
   - Click menu ☰ → **Discover** (hoặc **OpenSearch Dashboards → Discover**)

2. Chọn index pattern (nếu chưa chọn):
   - Ở góc trái, dropdown phía trên → chọn `spring-logs-*`

3. Chọn khoảng thời gian:
   - Ở góc phải trên, click vào **time picker** (biểu tượng đồng hồ / "Last 15 minutes")
   - Chọn **"Last 1 hour"** hoặc **"Today"**
   - Click **"Refresh"** hoặc **"Update"**

**Kết quả mong đợi:**  
- Thấy histogram (biểu đồ cột) hiển thị số lượng log theo thời gian
- Bên dưới là danh sách log entries

4. **Thực hành tìm kiếm (Search):**

Gõ vào ô search bar phía trên và nhấn Enter:

| Tìm kiếm | Kết quả mong đợi |
|---|---|
| `level:ERROR` | Chỉ hiển thị log ERROR |
| `level:WARN` | Chỉ hiển thị log WARN |
| `level:INFO` | Chỉ hiển thị log INFO |
| `logger_name:*DemoController*` | Log từ DemoController |
| `message:product` | Log chứa từ "product" (không phân biệt hoa thường) |
| `level:ERROR OR level:WARN` | Log ERROR hoặc WARN |

> **Tip:** Xóa search bar (hoặc gõ `*`) để quay lại xem tất cả log.

5. **Thực hành Filter:**
   - Ở danh sách log, click vào field `level` bên cột trái
   - Click giá trị `ERROR` → chọn **🔍+ (filter for value)** → chỉ hiển thị ERROR
   - Muốn bỏ filter → click dấu **✕** trên filter chip ở phía trên

6. **Thêm cột hiển thị:**
   - Danh sách fields bên trái → hover vào `level` → click **"Add"**
   - Làm tương tự cho: `logger_name`, `message`, `thread_name`
   - Bây giờ bảng hiển thị gọn hơn với các cột đã chọn

**Reply ngay:** "**Xong 6.2**" khi đã thử search, filter, và thêm cột.

---

#### **Part 6.3: Tạo Visualization 1 – Area Chart (Số request theo thời gian)**

1. Mở Visualize:
   - Click menu ☰ → **Visualize**
   - Click **"Create visualization"**

2. Chọn loại chart:
   - Chọn **"Area"** (biểu đồ vùng)

3. Chọn data source:
   - Chọn index pattern **`spring-logs-*`**

4. Cấu hình trục Y (Metrics):
   - Mặc định đã có **Count** → giữ nguyên (đếm số log)

5. Cấu hình trục X (Buckets):
   - Click **"Add"** trong phần **Buckets**
   - Chọn **"X-axis"**
   - **Aggregation:** `Date Histogram`
   - **Field:** `@timestamp`
   - **Minimum interval:** `Minute` (hoặc `Auto`)

6. Click nút **▶ (Apply changes)** ở phía trên để xem kết quả.

**Kết quả mong đợi:**  
Biểu đồ Area hiển thị số lượng log theo thời gian, dạng đường cong/vùng tô màu.

7. Lưu visualization:
   - Click **"Save"** (phía trên bên phải)
   - Tên: `Log Count Over Time`
   - Click **"Save"**

**Reply ngay:** "**Xong 6.3**" khi đã tạo và save.

---

#### **Part 6.4: Tạo Visualization 2 – Pie Chart (Phân bố log level)**

1. Tạo visualization mới:
   - Click menu ☰ → **Visualize** → **"Create visualization"**

2. Chọn loại chart:
   - Chọn **"Pie"** (biểu đồ tròn)

3. Chọn data source:
   - Chọn **`spring-logs-*`**

4. Cấu hình Metrics:
   - Mặc định **Count** → giữ nguyên

5. Cấu hình Buckets:
   - Click **"Add"** → chọn **"Split slices"**
   - **Aggregation:** `Terms`
   - **Field:** `level` (hoặc `level.keyword`)
   - **Size:** `5`
   - **Order By:** `Count`

6. Click **▶ (Apply changes)**

**Kết quả mong đợi:**  
Pie chart hiển thị phần trăm mỗi log level:
- 🟢 **INFO** – phần lớn (xanh)
- 🟡 **WARN** – một phần (vàng)
- 🔴 **ERROR** – phần nhỏ (đỏ)

7. Lưu:
   - Click **"Save"**
   - Tên: `Log Level Distribution`
   - Click **"Save"**

**Reply ngay:** "**Xong 6.4**" khi đã tạo và save.

---

#### **Part 6.5: Tạo Visualization 3 – Data Table (Top 10 Logger)**

1. Tạo visualization mới:
   - Click menu ☰ → **Visualize** → **"Create visualization"**

2. Chọn loại chart:
   - Chọn **"Data table"** (bảng dữ liệu)

3. Chọn data source:
   - Chọn **`spring-logs-*`**

4. Cấu hình Metrics:
   - Mặc định **Count** → giữ nguyên

5. Cấu hình Buckets:
   - Click **"Add"** → chọn **"Split rows"**
   - **Aggregation:** `Terms`
   - **Field:** `logger_name` (hoặc `logger_name.keyword`)
   - **Size:** `10`
   - **Order By:** `Count` (Descending)

6. Click **▶ (Apply changes)**

**Kết quả mong đợi:**  
Bảng hiển thị top 10 logger theo số lượng log:

| logger_name | Count |
|---|---|
| com.example.demo.DemoController | 45 |
| org.apache.catalina.core... | 5 |
| ... | ... |

7. Lưu:
   - Click **"Save"**
   - Tên: `Top 10 Loggers`
   - Click **"Save"**

**Reply ngay:** "**Xong 6.5**" khi đã tạo và save.

---

#### **Part 6.6: Tạo Visualization 4 – Metric (Tổng Error Count)**

1. Tạo visualization mới:
   - Click menu ☰ → **Visualize** → **"Create visualization"**

2. Chọn loại chart:
   - Chọn **"Metric"** (hiển thị số lớn)

3. Chọn data source:
   - Chọn **`spring-logs-*`**

4. Cấu hình Metrics:
   - Mặc định **Count** → giữ nguyên

5. Thêm filter để chỉ đếm ERROR:
   - Ở thanh search phía trên biểu đồ, gõ: `level:ERROR`
   - Nhấn **Enter** hoặc click **"Update"**

6. Click **▶ (Apply changes)**

**Kết quả mong đợi:**  
Một số lớn hiển thị tổng số log ERROR, ví dụ: **5**

7. Lưu:
   - Click **"Save"**
   - Tên: `Total Error Count`
   - Click **"Save"**

**Reply ngay:** "**Xong 6.6**" khi đã tạo và save.

---

#### **Part 6.7: Tạo Dashboard – gộp tất cả Visualizations**

1. Mở Dashboard:
   - Click menu ☰ → **Dashboard**
   - Click **"Create dashboard"** (hoặc **"Create new"**)

2. Thêm các visualizations đã tạo:
   - Click **"Add"** (hoặc **"Add an existing"**) ở thanh phía trên
   - Tìm và click lần lượt:
     - ✅ `Log Count Over Time` (Area chart)
     - ✅ `Log Level Distribution` (Pie chart)
     - ✅ `Top 10 Loggers` (Data table)
     - ✅ `Total Error Count` (Metric)
   - Đóng panel "Add" sau khi thêm xong

3. Sắp xếp layout:
   - **Kéo thả** (drag & drop) các visualization để sắp xếp:
   
   ```
   ┌──────────────────────────┬──────────────────┐
   │  Log Count Over Time     │  Total Error     │
   │  (Area - chiếm rộng)    │  Count (Metric)  │
   ├──────────────────────────┼──────────────────┤
   │  Log Level Distribution  │  Top 10 Loggers  │
   │  (Pie chart)            │  (Data table)    │
   └──────────────────────────┴──────────────────┘
   ```
   
   - **Resize**: kéo góc dưới phải của mỗi panel để thay đổi kích thước

4. Chọn time range:
   - Góc phải trên → chọn **"Last 1 hour"** hoặc **"Today"**
   - Click **"Refresh"**

5. Lưu Dashboard:
   - Click **"Save"** ở góc phải trên
   - Tên: `Spring Boot Logging Dashboard`
   - ✅ Check **"Store time with dashboard"** (tùy chọn)
   - Click **"Save"**

**Kết quả mong đợi:**  
Dashboard hiển thị 4 panels:
- 📊 Area chart: request theo thời gian
- 🥧 Pie chart: INFO/WARN/ERROR distribution
- 📋 Data table: top loggers
- 🔢 Metric: tổng số ERROR

**Reply ngay:** "**Xong 6.7**" khi dashboard hoàn tất.

---

#### **Part 6.8: Test Dashboard realtime (bonus)**

1. Giữ Dashboard mở trong browser.

2. Mở Terminal mới, gửi thêm requests:

```bash
# Tạo 30 requests mới
for i in $(seq 1 30); do
  curl -s http://localhost:8080/api/product/$((RANDOM % 1000)) > /dev/null
  sleep 0.2
done
echo "Done! 30 requests sent."
```

3. Quay lại browser, click **"Refresh"** trên Dashboard.

**Kết quả mong đợi:**  
- Area chart cập nhật thêm data mới
- Pie chart cập nhật tỷ lệ
- Error count có thể tăng
- Data table cập nhật count

4. (Tùy chọn) Bật Auto-refresh:
   - Click time picker → **"Auto-refresh"** hoặc chọn interval **"10 seconds"**
   - Dashboard sẽ tự cập nhật liên tục

---

**Phase 6 HOÀN TẤT!** 🎉

**Tổng kết Phase 6 – những gì đã làm:**
- ✅ Tạo Index Pattern `spring-logs-*` với time field `@timestamp`
- ✅ Sử dụng **Discover** để search, filter, và khám phá log
- ✅ Tạo **4 Visualizations:**

| # | Tên | Loại | Hiển thị |
|---|---|---|---|
| 1 | Log Count Over Time | Area Chart | Số log theo thời gian |
| 2 | Log Level Distribution | Pie Chart | Tỷ lệ INFO/WARN/ERROR |
| 3 | Top 10 Loggers | Data Table | Logger có nhiều log nhất |
| 4 | Total Error Count | Metric | Tổng số lỗi |

- ✅ Gộp 4 visualizations thành **Dashboard** hoàn chỉnh
- ✅ Test realtime data cập nhật

**Reply:** "**Xong Phase 6**" hoặc "**Xong 6.8**"  
Mình sẽ chuyển sang **Phase 7** (Hoàn tất & Next Steps).

---
