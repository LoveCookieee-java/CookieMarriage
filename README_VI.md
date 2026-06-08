<div align="center">

# 🍪 CookieMarriage — Hướng Dẫn Tiếng Việt

**Plugin kết hôn cho Paper 1.21+ | Kinh tế chung | Điểm danh streak hàng ngày**

</div>

---

## 📋 Yêu Cầu

| Thành phần | Phiên bản | Ghi chú |
|-----------|-----------|---------|
| **Paper** | 1.21+ | Không hỗ trợ Spigot |
| **Vault** | Bất kỳ | Cầu nối kinh tế |
| **Plugin kinh tế** | Bất kỳ | EssentialsX, CMI, ... |
| **Java** | 21+ | Phải khớp với JDK server |

---

## 📦 Cài Đặt

1. Tải `CookieMarriage-v1.0.jar` từ [Releases](../../releases)
2. Bỏ vào thư mục `plugins/` của server
3. Đảm bảo đã có **Vault** và plugin kinh tế (EssentialsX, CMI, ...)
4. Khởi động lại server
5. Chỉnh sửa `plugins/CookieMarriage/config.yml` và `reward.yml` theo ý muốn
6. Reload plugin hoặc restart lại server

---

## 🎮 Lệnh

| Lệnh | Mô tả | Permission |
|------|-------|------------|
| `/marry <player>` | Gửi lời cầu hôn đến người chơi | `marriage.marry` |
| `/marry accept` | Chấp nhận lời cầu hôn | `marriage.marry` |
| `/marry deny` | Từ chối lời cầu hôn | `marriage.marry` |
| `/divorce` | Ly hôn (chia đôi tiền 50/50) | `marriage.divorce` |
| `/marriage balance` | Xem tổng số dư Vault của cặp đôi | `marriage.use` |
| `/marriage info` | Xem streak, thời gian chơi hôm nay, ngày kết hôn | `marriage.use` |
| `/marriage help` | Hiện hướng dẫn | `marriage.use` |

---

## 🔑 Quyền Hạn

| Permission | Mặc định | Mô tả |
|-----------|---------|-------|
| `marriage.marry` | `true` | Dùng `/marry` |
| `marriage.divorce` | `true` | Dùng `/divorce` |
| `marriage.use` | `true` | Dùng các lệnh `/marriage` |

---

## ⚙️ Cấu Hình Chi Tiết

### `config.yml`

```yaml
economy:
  notify-transactions: true
  # Có gửi thông báo cho cặp đôi khi số dư thay đổi hay không

  poll-interval-ticks: 200
  # Tần suất kiểm tra số dư Vault (đơn vị: ticks, 200 ticks = 10 giây)
  # Tăng giá trị này nếu server bị lag

  notify-threshold: 0.01
  # Mức thay đổi tối thiểu (VNĐ/số tiền) để kích hoạt thông báo
  # Tránh thông báo nhiễu từ sai số floating point

  split-on-divorce: true
  # Có chia đều tiền khi ly hôn hay không

proposal:
  timeout-seconds: 60
  # Lời cầu hôn hết hạn sau bao nhiêu giây nếu không được chấp nhận

attendance:
  required-minutes: 60
  # Số phút cả hai phải cùng online trong một ngày để được điểm danh

  check-interval-ticks: 1200
  # Tần suất kiểm tra (đơn vị: ticks, 1200 ticks = 60 giây)

  timezone: "Asia/Ho_Chi_Minh"
  # Múi giờ để tính ngày điểm danh (mặc định: giờ Việt Nam UTC+7)
  # Danh sách timezone: https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
```

### `messages.yml`

Tất cả tin nhắn hỗ trợ mã màu `&`:

```yaml
prefix: "&6[&eCookieMarriage&6] &r"
marry:
  proposal-sent: "%prefix%&aBạn đã gửi lời cầu hôn đến &e%player%&a."
  ...
```

---

## 🎁 Cấu Hình Phần Thưởng (`reward.yml`)

Cấu hình lệnh tự động khi cặp đôi đạt mốc streak:

```yaml
milestone:
  1:
    - "eco give %couple% 500"
  7:
    - "eco give %couple% 2000"
    - "broadcast &e%player1% &avà &e%player2% &ađã chơi cùng nhau 7 ngày!"
  30:
    - "eco give %couple% 10000"
  100:
    - "eco give %couple% 50000"
```

### Placeholder trong lệnh phần thưởng

| Placeholder | Giải thích |
|-------------|-----------|
| `%couple%` | Lệnh chạy **2 lần** — một lần cho mỗi người trong cặp |
| `%player1%` | Tên người chơi 1 |
| `%player2%` | Tên người chơi 2 |
| `%streak%` | Số streak hiện tại |

> **Ví dụ:** `eco give %couple% 1000` sẽ chạy:<br>
> `eco give Player1 1000` → rồi → `eco give Player2 1000`

---

## 📅 Hệ Thống Streak & Điểm Danh

### Cách hoạt động

1. Plugin kiểm tra mỗi `check-interval-ticks` ticks (mặc định 60s)
2. Nếu **cả hai** người trong cặp đôi cùng online → đếm thêm 1 phút
3. Khi đạt `required-minutes` phút (mặc định 60 phút) trong ngày → **điểm danh thành công**
4. Streak tăng 1, plugin kiểm tra phần thưởng tương ứng
5. **Midnight reset (giờ Việt Nam):** Nếu hôm qua không điểm danh → streak về 0

### Lưu ý quan trọng

- Streak chỉ tính khi **cả hai đều online cùng lúc**
- Nếu một người đăng xuất, bộ đếm phiên về 0
- Điểm danh tính theo **ngày Việt Nam** (UTC+7) — ngay cả server ở múi giờ khác

---

## 💰 Hệ Thống Kinh Tế Chung

### Cách hoạt động (Option A — Polling)

Plugin **không** trực tiếp can thiệp vào Vault mà theo dõi bằng cách polling:

1. Mỗi `poll-interval-ticks` ticks, plugin đọc số dư Vault của cả hai
2. So sánh với số dư lần trước đã lưu
3. Nếu thay đổi > `notify-threshold` → gửi thông báo cho cả hai người

### Khi ly hôn

- Tính tổng `balance_p1 + balance_p2`
- Chia đôi → mỗi người nhận `total / 2`
- Nếu một người có nhiều hơn phần của họ → rút bớt
- Nếu ít hơn → cộng thêm

---

## 🗄️ Dữ Liệu

Plugin lưu dữ liệu vào SQLite tại `plugins/CookieMarriage/marriage.db`.

**Không cần cài MySQL hay bất kỳ database nào khác.**

Backup: Chỉ cần copy file `marriage.db` là đủ.

---

## ❓ Câu Hỏi Thường Gặp

**Q: Plugin có hỗ trợ Spigot không?**  
A: Không. Plugin dùng Adventure API của Paper và một số API 1.21+.

**Q: Streak có reset khi server restart không?**  
A: Không. Dữ liệu được lưu realtime vào SQLite sau mỗi sự kiện.

**Q: Một người chơi có thể kết hôn với nhiều người không?**  
A: Không. Mỗi người chỉ có thể trong một mối quan hệ tại một thời điểm.

**Q: Lệnh trong reward.yml có cần có `/` không?**  
A: Không. Viết `eco give %couple% 1000` không có dấu `/`.

**Q: Có thể đặt streak thủ công không?**  
A: Hiện tại chưa có lệnh admin. Có thể chỉnh trực tiếp trong `marriage.db`.

---

## 🐛 Báo Lỗi

Tạo issue tại: [GitHub Issues](../../issues)

Vui lòng đính kèm:
- Phiên bản Paper/Spigot
- Log từ `logs/latest.log` (phần lỗi)
- Nội dung `config.yml`

---

## 📄 Giấy Phép

MIT License — Xem [LICENSE](LICENSE) để biết thêm chi tiết.
