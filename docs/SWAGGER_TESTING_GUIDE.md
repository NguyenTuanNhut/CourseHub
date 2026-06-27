# Hướng Dẫn Kiểm Thử Từng Bước (API Testing Guide) qua Swagger UI

Tài liệu này được thiết kế dành riêng cho bạn, giải thích từ những khái niệm cơ bản nhất cho đến cách thức kiểm thử thực tế từng vai trò (**Admin**, **Giảng viên**, **Sinh viên**) trên giao diện Swagger UI.

---

## PHẦN 1: CÁC KHÁI NIỆM CƠ BẢN (DÀNH CHO NGƯỜI MỚI)

### 1. API và Swagger UI là gì?
* **API (Application Programming Interface)**: Giống như một "thực đơn" trong nhà hàng. Giao diện frontend (hoặc ứng dụng điện thoại) là người gọi món, và backend (chính là dự án Java này) là đầu bếp trong bếp. API là các đường link (được gọi là các **endpoint**) để truyền nhận dữ liệu giữa giao diện và cơ sở dữ liệu.
* **Swagger UI**: Là một trang web tự động hiển thị danh sách tất cả các "món ăn" (API) có trong dự án của bạn. Nó cho phép bạn bấm nút và điền thông tin để "gọi món" trực tiếp trên trình duyệt mà không cần phải viết code giao diện.

### 2. Các phương thức gọi API (HTTP Methods)
Khi nhìn vào Swagger, bạn sẽ thấy các nhãn màu khác nhau:
* **GET** (Màu xanh dương): Dùng để **Lấy dữ liệu** (ví dụ: Xem danh sách môn học, xem điểm).
* **POST** (Màu xanh lá): Dùng để **Tạo mới dữ liệu** hoặc **Gửi yêu cầu bảo mật** (ví dụ: Đăng ký môn học, Đăng nhập, Nộp bài).
* **PUT / PATCH** (Màu cam/vàng): Dùng để **Cập nhật sửa đổi dữ liệu** (ví dụ: Sửa thông tin môn học, sửa điểm).
* **DELETE** (Màu đỏ): Dùng để **Xóa dữ liệu**.

### 3. Cơ chế bảo mật JWT Token (Bearer Authorization)
Để bảo mật hệ thống, ứng dụng sử dụng cơ chế **JWT (JSON Web Token)**:
1. Khi bạn gửi đúng tài khoản và mật khẩu đến API **Login**, máy chủ sẽ trả về một chuỗi ký tự dài (gọi là **AccessToken**).
2. Chuỗi này giống như một "vé vào cổng VIP" có thời hạn.
3. Mỗi khi bạn muốn gọi các API bảo mật (ví dụ: chấm điểm, nộp bài), bạn phải trình tấm vé này bằng cách dán nó vào nút **Authorize** ở đầu trang Swagger theo định dạng: `Bearer <chuỗi_token>`.

---

## PHẦN 2: THÔNG TIN CÁC TÀI KHOẢN MẪU (ĐÃ TỰ ĐỘNG TẠO SẴN)

Hệ thống đã tự động tạo sẵn 3 tài khoản trong bộ nhớ RAM để bạn thử nghiệm ngay lập tức:

| Vai trò (Role) | Username | Password | Email mẫu | Chức năng chính |
| :--- | :--- | :--- | :--- | :--- |
| **ADMIN** | `admin` | `Admin@123` | `admin@coursehub.com` | Quản lý người dùng, tạo môn học, phân công giảng viên |
| **LECTURER** (Giảng viên) | `lecturer01` | `Lecturer@123` | `lecturer01@coursehub.com` | Tạo bài tập, đăng tài liệu, chấm điểm sinh viên |
| **STUDENT** (Sinh viên) | `student01` | `Student@123` | `student01@coursehub.com` | Đăng ký học, tải tài liệu, nộp bài tập, xem điểm |

---

## PHẦN 3: KỊCH BẢN KIỂM THỬ CHI TIẾT TỪNG CHỨC NĂNG

Hãy thực hiện theo đúng thứ tự luồng dưới đây để hiểu cách vận hành của hệ thống:

### LUỒNG 1: ĐĂNG NHẬP VÀ ĐĂNG KÝ (MỌI NGƯỜI DÙNG)

#### 1. Đăng ký tài khoản Sinh viên mới (Public Register)
* **API**: `POST /api/v1/auth/register/students` (Màu xanh lá)
* **Ý nghĩa**: Cho phép người dùng tự đăng ký một tài khoản sinh viên mới.
* **Cách test**:
  1. Tìm API này trong nhóm **Authentication Controller**.
  2. Bấm **Try it out**.
  3. Sửa thông tin trong Body thành:
     ```json
     {
       "username": "student_test",
       "email": "test@gmail.com",
       "password": "Password@123",
       "confirmPassword": "Password@123",
       "fullName": "Nguyen Van A",
       "phone": "0987654321"
     }
     ```
  4. Bấm **Execute**. Bạn sẽ nhận về kết quả thành công với mã `201 Created`.

#### 2. Đăng nhập hệ thống (Login)
* **API**: `POST /api/v1/auth/login`
* **Ý nghĩa**: Gửi mật khẩu lên để lấy chìa khóa Access Token.
* **Cách test**:
  1. Chọn API login.
  2. Nhập tài khoản admin:
     ```json
     {
       "username": "admin",
       "password": "Admin@123"
     }
     ```
  3. Bấm **Execute**.
  4. Cuộn xuống phần Response Body, bạn sẽ thấy:
     ```json
     "data": {
       "accessToken": "eyJhbGciOiJI...",
       "refreshToken": "eyJhbGciOiJI...",
       ...
     }
     ```
  5. Hãy **copy toàn bộ giá trị của `accessToken`** (bôi đen chuỗi ký tự dài nằm giữa hai dấu ngoặc kép và copy).

#### 3. Bật khóa bảo mật (Authorize)
* **Cách làm**:
  1. Cuộn chuột lên đầu trang Swagger.
  2. Bấm vào nút **Authorize** màu xanh lá.
  3. Ở ô nhập liệu, **CHỈ dán đoạn token dài đã copy vào** (KHÔNG gõ thêm chữ `Bearer` hay khoảng trắng nào ở trước, vì giao diện Swagger UI sẽ tự động thêm chữ `Bearer` cho bạn).
     * *Ví dụ nhập vào:* `eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`
  4. Bấm **Authorize** -> Bấm **Close**.
  *(Từ lúc này, bạn đang thao tác dưới danh nghĩa quyền quản trị cao nhất - **Admin**).*

---

### LUỒNG 2: VAI TRÒ ADMIN (QUẢN TRỊ VIÊN)

Sau khi đã Đăng nhập và Authorize bằng tài khoản `admin`:

#### 1. Tạo một khóa học mới
* **API**: `POST /api/v1/admin/courses` (Nằm trong **Admin Course Controller**)
* **Ý nghĩa**: Tạo môn học mới và phân công Giảng viên giảng dạy.
* **Cách test**:
  1. Chọn API trên -> Bấm **Try it out**.
  2. Nhập thông tin môn học. Lưu ý phân công cho giảng viên `lecturer01` (ID là `2` vì hệ thống tự động gán ID `1` cho Admin, ID `2` cho Lecturer, ID `3` cho Student):
     ```json
     {
       "courseCode": "JAVA101",
       "courseName": "Lap Trinh Java Co Ban",
       "description": "Mon hoc giup lam quen voi Java",
       "credit": 3,
       "lecturerId": 2,
       "maximumStudents": 40,
       "startDate": "2026-07-01",
       "endDate": "2026-09-30"
     }
     ```
  3. Bấm **Execute**. Bạn sẽ thấy trả về mã `201` kèm theo thông tin khóa học mới được tạo ra có **ID là `1`**.

#### 2. Kích hoạt trạng thái khóa học
* **API**: `PUT /api/v1/admin/courses/{id}/status`
* **Ý nghĩa**: Mở lớp học để học sinh có thể đăng ký (Chuyển trạng thái sang `OPEN`).
* **Cách test**:
  1. Điền vào ô `id` giá trị: `1` (ID khóa học vừa tạo ở bước trên).
  2. Điền ô `status` giá trị chữ hoa: `OPEN`.
  3. Bấm **Execute**. Trả về thành công `200 OK`. Lúc này lớp học Java đã mở đăng ký công khai.

---

### LUỒNG 3: VAI TRÒ SINH VIÊN (STUDENT)

Bây giờ bạn muốn đóng vai học sinh để đăng ký môn học và nộp bài.

#### 1. Đổi quyền sang Student
1. Nhấn nút **Authorize** ở đầu trang -> Bấm **Logout** để xóa quyền Admin cũ.
2. Xuống **Authentication Controller** -> Gửi request đăng nhập với tài khoản sinh viên:
   ```json
   {
     "username": "student01",
     "password": "Student@123"
   }
   ```
3. Copy đoạn `accessToken` mới trả về.
4. Lên đầu trang bấm **Authorize** -> Gõ `Bearer ` + dán token sinh viên vào -> Bấm **Authorize** -> **Close**.

#### 2. Đăng ký học môn lập trình Java
* **API**: `POST /api/v1/student/courses/{courseId}/enrollments`
* **Ý nghĩa**: Sinh viên tự đăng ký vào lớp học Java có ID `1`.
* **Cách test**:
  1. Nhập `courseId` là `1`.
  2. Bấm **Execute**. Trả về mã thành công `201 Created` thể hiện sinh viên `student01` đã đăng ký học môn này thành công.

---

### LUỒNG 4: VAI TRÒ GIẢNG VIÊN (LECTURER)

Giảng viên cần tạo bài tập lớn cho sinh viên làm và đăng tài liệu học tập.

#### 1. Đổi quyền sang Giảng viên
1. Bấm **Authorize** -> Chọn **Logout** (xóa quyền của sinh viên).
2. Đăng nhập bằng giảng viên:
   ```json
   {
     "username": "lecturer01",
     "password": "Lecturer@123"
   }
   ```
3. Copy `accessToken` của giảng viên.
4. Lên đầu trang bấm **Authorize** -> Gõ `Bearer ` + dán token giảng viên vào -> Bấm **Authorize** -> **Close**.

#### 2. Đăng bài tập cho lớp
* **API**: `POST /api/v1/lecturer/courses/{courseId}/assignments`
* **Ý nghĩa**: Ra bài tập cho lớp học Java (ID `1`).
* **Cách test**:
  1. Nhập `courseId` là `1` ở phần Path parameter.
  2. Nhập body thông tin bài tập (mở nộp từ hôm nay đến cuối tháng):
     ```json
     {
       "title": "Bai tap lon OOP Java",
       "description": "Xay dung ung dung quan ly diem sinh vien",
       "instructions": "Nop ma nguon Github va bao cao PDF",
       "maximumScore": 100.0,
       "openAt": "2026-06-26T00:00:00Z",
       "dueAt": "2026-07-31T23:59:59Z",
       "allowLateSubmission": true
     }
     ```
  3. Bấm **Execute**. Bạn nhận lại thông tin bài tập được tạo ra có **ID là `1`**.

---

### LUỒNG 5: SINH VIÊN NỘP BÀI & GIẢNG VIÊN CHẤM ĐIỂM

Đây là luồng tương tác quan trọng nhất của hệ thống:

#### 1. Sinh viên nộp bài làm
1. **Đổi quyền** sang tài khoản Sinh viên (`student01` / `Student@123`) bằng cách Logout token cũ và Authorize token mới của sinh viên.
2. Tìm API **Student Course Controller** -> `POST /api/v1/student/submissions`.
3. Nhập dữ liệu bài nộp (ví dụ nộp link Github và link tài liệu bài báo cáo):
   ```json
   {
     "assignmentId": 1,
     "githubUrl": "https://github.com/student01/oop-project",
     "reportUrl": "https://drive.google.com/student01/report-pdf",
     "originalFileName": "oop_report.pdf",
     "fileType": "pdf"
   }
   ```
4. Bấm **Execute**. Hệ thống ghi nhận bài nộp thành công và cấp mã bài nộp **ID là `1`**.

#### 2. Giảng viên chấm điểm bài nộp
1. **Đổi quyền** sang tài khoản Giảng viên (`lecturer01` / `Lecturer@123`).
2. Tìm API **Lecturer Course Controller** -> `POST /api/v1/lecturer/grades`.
3. Nhập điểm và lời phê bình của giảng viên cho bài nộp ID `1`:
   ```json
   {
     "submissionId": 1,
     "score": 95.5,
     "feedback": "Code rat tot, cau truc OOP ro rang, bao cao chi tiet."
   }
   ```
4. Bấm **Execute**. Điểm số được lưu lại và tự động ghi vết (Log) thời gian thực hiện qua AOP.

#### 3. Sinh viên xem điểm của mình
1. **Đổi quyền** sang tài khoản Sinh viên (`student01`).
2. Tìm API **Student Course Controller** -> `GET /api/v1/student/submissions/{id}/grade` (Với `id` là ID của bài nộp của sinh viên - số `1`).
3. Bấm **Execute**. Bạn sẽ nhận về thông tin điểm số: `95.5` kèm lời phê bình của Giảng viên.

---

## PHẦN 4: HƯỚNG DẪN XỬ LÝ LỖI THƯỜNG GẶP (TROUBLESHOOTING)

* **Lỗi `401 Unauthorized`**: Bạn chưa bấm nút **Authorize** ở đầu trang hoặc chuỗi nhập vào thiếu chữ `Bearer ` hoặc token đã hết hạn (hạn dùng mặc định của access token là 15 phút). Hãy đăng nhập lại để lấy token mới.
* **Lỗi `403 Forbidden`**: Bạn đang dùng sai vai trò để truy cập API. Ví dụ: Dùng tài khoản Sinh viên để truy cập API của Giảng viên (nhóm `/api/v1/lecturer/**`). Hãy đổi đúng token của vai trò được cấp quyền.
* **Lỗi `400 Bad Request`**: Dữ liệu bạn điền vào JSON bị sai định dạng (ví dụ: ngày tháng không đúng mẫu `YYYY-MM-DD`, thiếu các trường bắt buộc, hoặc nhập điểm chấm vượt quá `100.0`). Hãy xem thông báo lỗi cụ thể ở khung response để sửa đổi.
* **Mất dữ liệu sau khi tắt server**: Vì chúng ta đang dùng database H2 (In-memory) để tránh cài đặt phức tạp, nên mỗi lần bạn tắt ứng dụng và khởi chạy lại, toàn bộ dữ liệu bạn tự tạo thêm sẽ bị xóa sạch và reset lại 3 tài khoản mặc định lúc ban đầu.
