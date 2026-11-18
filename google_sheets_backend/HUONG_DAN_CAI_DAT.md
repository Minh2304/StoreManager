# Hướng Dẫn Chạy Ứng Dụng Trên Android Studio & Kết Nối Google Sheets

Dự án **AFaDa Đồ Uống - Quản Lý Bán Hàng (Bia - Rượu - Nước ngọt)** được thiết kế **tối giản, chữ to, ít chữ, dễ nhìn cho người lớn tuổi**, hỗ trợ:
1. **Đăng nhập bằng Gmail** trên nhiều thiết bị (**không cần xác thực OTP khi đăng nhập**; chỉ khi **Đăng ký mới** hoặc **Quên mật khẩu** mới cần mã xác thực 6 số).
2. **Trang Chọn / Thêm Cửa Hàng (Database):** Hiển thị danh sách các cửa hàng hiện có của Gmail và **nút dấu `(+)` màu cam cực lớn ở góc dưới** để đặt tên và tự động khởi tạo Database mới trên Gmail (Google Drive / Google Sheets) của người dùng.

---

## Phần 1: Cách mở và chạy trên máy ảo Android Studio

1. Mở phần mềm **Android Studio**.
2. Chọn **Open** $\rightarrow$ Trỏ tới thư mục: `D:\Python\StoreManager` và bấm **OK**.
3. Đợi Android Studio tải thư viện (**Gradle Sync**).
4. Bấm nút **Run 'app'** (`▶` màu xanh lá trên thanh công cụ) để chạy trên máy ảo.

### Cách dùng thử ngay trên máy ảo:
* **Bước 1 (Đăng nhập):** Nhập bất kỳ địa chỉ Gmail nào của bạn (ví dụ: `dailybia@gmail.com`) và mật khẩu $\rightarrow$ Bấm nút cam **VÀO ỨNG DỤNG** (đăng nhập trực tiếp, không cần mã xác thực).
  * Nếu muốn thử tính năng xác thực Gmail: Bấm **Đăng ký mới** hoặc **Quên mật khẩu** $\rightarrow$ Bấm **Lấy mã** để nhận mã xác thực 6 số.
* **Bước 2 (Thêm Database):** Sau khi đăng nhập, ứng dụng chuyển tới trang **CHỌN CỬA HÀNG**. Bấm vào nút dấu **`(+)`** ở góc dưới $\rightarrow$ Nhập tên cửa hàng (ví dụ: `Đại Lý Bia Hùng Phát`) $\rightarrow$ Bấm **TẠO NGAY**.
* **Bước 3 (Bán hàng):** Hệ thống tự động tạo Database với 10 mặt hàng Bia - Rượu - Nước ngọt mẫu (Tiger, Heineken, Sài Gòn, Coca, Pepsi, RedBull...) và mở thẳng vào màn hình quản lý bán hàng. Bạn có thể bấm nút **"Đổi quán"** ở góc trên màn hình Trang chủ để quay lại danh sách Database hoặc tạo thêm cửa hàng mới bất kỳ lúc nào.

---

## Phần 2: Cách gắn Google Apps Script để tự động tạo file Google Sheet trên Gmail

1. Mở [Google Sheets](https://sheets.new) để tạo một bảng tính tổng (Master Sheet).
2. Trên thanh menu, chọn **Tiện ích mở rộng (Extensions)** $\rightarrow$ **Apps Script**.
3. Copy toàn bộ mã trong file [Code.gs](file:///d:/Python/StoreManager/google_sheets_backend/Code.gs) dán vào cửa sổ Apps Script.
4. Bấm **Lưu (Save)**, chọn hàm `setupSheets` và bấm **Chạy (Run)** (Cấp quyền cho phép gửi email OTP và tạo file Spreadsheet khi Google hỏi).
5. Bấm **Triển khai (Deploy)** $\rightarrow$ **Tùy chọn triển khai mới (New deployment)**:
   * **Chọn loại:** Ứng dụng web (*Web app*).
   * **Thực thi dưới dạng:** Tôi (*Me*).
   * **Ai có quyền truy cập:** Bất kỳ ai (*Anyone*).
6. Copy đường dẫn **Web App URL** (có đuôi `/exec`) và dán vào mục **Database** trong Cài đặt. Từ lúc này, mỗi khi người dùng nhấn nút `Thêm cửa hàng`, Google Apps Script sẽ tự động dùng lệnh `SpreadsheetApp.create()` tạo một file Google Sheet riêng theo đúng tên cửa hàng và chia sẻ thẳng vào Gmail của người dùng!

---

## Phần 3: Cách Cập Nhật Ứng Dụng Từ Xa Qua Nhánh Git `release` (OTA Update)

Khi bạn code trên máy tính từ xa và muốn cài đặt / cập nhật ứng dụng lên điện thoại Android ở xa:

1. **Quy trình phát hành bản cập nhật mới từ máy tính:**
   * Mở file [`version.json`](file:///d:/Python/StoreManager/version.json) ở thư mục gốc của dự án và tăng số `versionCode` (ví dụ từ `1` lên `2`), đổi `versionName` (ví dụ `"1.0.1"`) và ghi chú `releaseNotes`.
   * Đẩy code lên nhánh `release` trên GitHub (`https://github.com/Minh2304/StoreManager`):
     ```powershell
     git checkout -B release
     git add .
     git commit -m "Release v1.0.1"
     git push -u origin release
     ```
   * Ngay khi bạn push lên nhánh `release`, workflow tự động [`.github/workflows/android-release.yml`](file:///d:/Python/StoreManager/.github/workflows/android-release.yml) trên GitHub Actions sẽ tự động biên dịch ra file `AFaDa-StoreManager.apk` và xuất bản vào mục **GitHub Releases (`latest`)**.

2. **Nhận thông báo và cập nhật trên điện thoại ở xa:**
   * Khi mở ứng dụng trên điện thoại, ứng dụng tự động đọc file `version.json` trên nhánh `release`.
   * Nếu phát hiện có phiên bản mới, **biểu tượng chuông Thông báo** (nằm ngay bên trái nút Cài đặt ở góc trên bên phải Trang chủ) sẽ hiện **chấm đỏ**.
   * Người dùng chỉ cần chạm vào biểu tượng Thông báo $\rightarrow$ Bấm **`Cập nhật ngay`** (hoặc vào **Cài đặt $\rightarrow$ Cài đặt chung $\rightarrow$ Thông tin phần mềm $\rightarrow$ Tải APK mới**) để tải và cài đè bản mới nhất trực tiếp trên điện thoại!
