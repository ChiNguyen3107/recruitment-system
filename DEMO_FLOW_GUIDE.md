# 🎬 HƯỚNG DẪN DEMO HỆ THỐNG TUYỂN DỤNG

## 📋 TỔNG QUAN DEMO

**Mục tiêu**: Demo đầy đủ hệ thống tuyển dụng với 3 vai trò chính: Quản trị viên, Nhà tuyển dụng, Ứng viên

**Thời gian**: 15-20 phút

**Công cụ**: Postman Collection + Cơ sở dữ liệu

**Cơ sở dữ liệu**: MySQL với 15 bảng chính, 45+ API endpoints

---

## 🎯 KỊCH BẢN DEMO

### **Tình huống**: Công ty TechCorp cần tuyển Lập trình viên Java Senior

### **Dữ liệu có sẵn trong hệ thống**:
- **5 công ty**: Tech Innovate Co., Digital Solutions Ltd., StartUp Hub, Tech Company Ltd
- **6 người dùng**: Admin, Employer, Recruiter, 3 Applicants
- **4 tin tuyển dụng**: Senior Java Developer, Lập trình viên Java cấp cao
- **2 đơn ứng tuyển**: Đã có timeline và phỏng vấn
- **8 kỹ năng**: Java, Spring Boot, React, MySQL, JavaScript, Python, Docker, Kubernetes

---

## 🚀 FLOW DEMO CHI TIẾT - LUỒNG MỚI

### **PHẦN 1: THIẾT LẬP & QUẢN TRỊ VIÊN (3 phút)**

#### 1.1 **Khởi động hệ thống**
```bash
# Chạy server
./START_SERVER.bat
# Hoặc
java -jar target/recruitment-system-1.0.0.jar
```

#### 1.2 **Đăng nhập với tài khoản Admin có sẵn**
```json
POST /api/auth/login
{
  "email": "admin@recruitment.com",
  "password": "password123"
}
```

**Lưu ý**: Tài khoản admin đã có sẵn trong hệ thống với:
- Email: `admin@recruitment.com`
- Role: `ADMIN`
- Status: `ACTIVE`
- Email đã verified

#### 1.3 **Xem Bảng điều khiển Quản trị**
```json
GET /api/admin/dashboard
Authorization: Bearer {token_admin}
```

**Kết quả mong đợi**: 
- Tổng số users: 6
- Tổng số companies: 5
- Tổng số jobs: 4
- Tổng số applications: 2
- Thống kê theo role và status

---

### **PHẦN 2: THIẾT LẬP NHÀ TUYỂN DỤNG (5 phút)**

#### 2.1 **Đăng nhập với tài khoản Employer có sẵn**
```json
POST /api/auth/login
{
  "email": "nguyenb2110051@student.ctu.edu.vn",
  "password": "password123"
}
```

**Lưu ý**: Tài khoản employer đã có sẵn với:
- Email: `nguyenb2110051@student.ctu.edu.vn`
- Name: Doan Chi Nguyen
- Role: `EMPLOYER`
- Company ID: 5 (Tech Company Ltd)
- Status: `ACTIVE`
- Phone: 0835886837

#### 2.2 **Xem thông tin công ty hiện tại**
```json
GET /api/companies/my
Authorization: Bearer {token_nha_tuyen_dung}
```

**Kết quả**: Công ty "Tech Company Ltd" đã có thông tin cơ bản

#### 2.3 **Cập nhật thông tin công ty**
```json
PUT /api/companies/my
Authorization: Bearer {token_nha_tuyen_dung}
{
  "name": "Công ty Công nghệ TechCorp",
  "description": "Công ty công nghệ hàng đầu chuyên về phát triển phần mềm và giải pháp AI",
  "website": "https://techcorp.vn",
  "industry": "Công nghệ",
  "companySize": "MEDIUM",
  "address": "123 Nguyễn Huệ, Quận 1, TP.HCM",
  "phoneNumber": "+84 28 1234 5678",
  "email": "hr@techcorp.vn",
  "benefits": ["Bảo hiểm sức khỏe", "Làm việc từ xa", "Lương tháng 13"],
  "workingHours": "9:00-18:00"
}
```

#### 2.4 **Xem Bảng điều khiển Nhà tuyển dụng**
```json
GET /api/employer/dashboard
Authorization: Bearer {token_nha_tuyen_dung}
```

**Kết quả mong đợi**:
- Số jobs đã tạo: 0 (chưa có)
- Số applications: 0
- Số interviews: 0
- Thống kê theo status

---

### **PHẦN 3: TẠO TIN TUYỂN DỤNG MỚI (4 phút)**

#### 3.1 **Xem danh sách jobs hiện có**
```json
GET /api/jobs/manage
Authorization: Bearer {token_nha_tuyen_dung}
```

**Kết quả**: Chưa có jobs nào (danh sách trống)

#### 3.2 **Tạo tin tuyển dụng mới**
```json
POST /api/jobs
Authorization: Bearer {token_nha_tuyen_dung}
{
  "title": "Lập trình viên Java Senior",
  "description": "Chúng tôi đang tìm kiếm lập trình viên Java có kinh nghiệm để tham gia đội ngũ phát triển sản phẩm công nghệ cao. Bạn sẽ làm việc trong môi trường năng động, có cơ hội phát triển nghề nghiệp và học hỏi các công nghệ mới nhất.",
  "requirements": "• Tốt nghiệp Đại học ngành Công nghệ thông tin hoặc tương đương\n• 5+ năm kinh nghiệm phát triển ứng dụng Java\n• Thành thạo Spring Boot, Spring Security, Spring Data JPA\n• Có kinh nghiệm với kiến trúc microservices\n• Biết sử dụng MySQL, PostgreSQL\n• Có kinh nghiệm với Docker, Kubernetes\n• Giao tiếp tiếng Anh tốt (TOEIC 600+)\n• Có kinh nghiệm làm việc nhóm và quản lý dự án",
  "responsibilities": "• Phát triển và duy trì các ứng dụng Java backend\n• Thiết kế và triển khai kiến trúc microservices\n• Tối ưu hóa hiệu suất và bảo mật hệ thống\n• Hướng dẫn và đào tạo lập trình viên junior\n• Tham gia code review và đảm bảo chất lượng code\n• Phối hợp với team frontend và DevOps",
  "benefits": "• Mức lương cạnh tranh: 25-40 triệu VND\n• Bảo hiểm sức khỏe toàn diện\n• Lương tháng 13 và thưởng KPI\n• Làm việc từ xa linh hoạt (2 ngày/tuần)\n• Đào tạo và phát triển kỹ năng chuyên môn\n• Môi trường làm việc trẻ trung, năng động\n• Cơ hội thăng tiến và phát triển nghề nghiệp",
  "jobType": "FULL_TIME",
  "workMode": "HYBRID",
  "experienceLevel": "SENIOR",
  "salaryMin": 25000000,
  "salaryMax": 40000000,
  "location": "TP. Hồ Chí Minh",
  "applicationDeadline": "2024-12-31T23:59:59",
  "status": "DRAFT"
}
```

#### 3.3 **Cập nhật trạng thái job thành ACTIVE**
```json
PUT /api/jobs/{jobId}/status
Authorization: Bearer {token_nha_tuyen_dung}
{
  "status": "ACTIVE"
}
```

#### 3.4 **Xem tin tuyển dụng đã tạo**
```json
GET /api/jobs/{jobId}
Authorization: Bearer {token_nha_tuyen_dung}
```

**Kết quả mong đợi**: Job mới với:
- Title: "Lập trình viên Java Senior"
- Company: Công ty Công nghệ TechCorp
- Salary: 25,000,000 - 40,000,000 VND
- Location: TP. Hồ Chí Minh
- Status: ACTIVE
- Applications: 0

---

### **PHẦN 4: LUỒNG ỨNG VIÊN MỚI (6 phút)**

#### 4.1 **Tạo tài khoản Ứng viên mới**
```json
POST /api/auth/register
{
  "email": "ungvien.moi@example.com",
  "password": "UngVien123!",
  "firstName": "Trần",
  "lastName": "Văn A",
  "role": "APPLICANT"
}
```

**Kết quả**: Tài khoản mới được tạo với:
- Email: `ungvien.moi@example.com`
- Name: Trần Văn A
- Role: `APPLICANT`
- Status: `PENDING` (chờ verify email)

#### 4.2 **Verify email và đăng nhập**
```json
POST /api/auth/verify-email
{
  "token": "mã_xác_minh_từ_email"
}

POST /api/auth/login
{
  "email": "ungvien.moi@example.com",
  "password": "UngVien123!"
}
```

#### 4.3 **Tạo hồ sơ cá nhân**
```json
PUT /api/profile
Authorization: Bearer {token_ung_vien}
{
  "firstName": "Trần",
  "lastName": "Văn A",
  "phoneNumber": "+84 901 234 567",
  "address": "456 Lê Lợi, Quận 3, TP.HCM",
  "bio": "Lập trình viên Java có kinh nghiệm 6 năm trong phát triển phần mềm doanh nghiệp. Có kinh nghiệm với Spring Boot, microservices và các công nghệ cloud hiện đại.",
  "skills": ["Java", "Spring Boot", "Spring Security", "Microservices", "Docker", "Kubernetes", "MySQL", "PostgreSQL", "Redis", "RabbitMQ"],
  "experience": "6 năm kinh nghiệm phát triển phần mềm",
  "education": "Cử nhân Khoa học Máy tính - Đại học Bách Khoa TP.HCM",
  "certifications": ["Oracle Certified Professional Java SE 8", "AWS Certified Developer", "Spring Professional Certification"],
  "languages": "Tiếng Việt (bản ngữ), Tiếng Anh (TOEIC 750)",
  "desiredSalaryMin": 25000000,
  "desiredSalaryMax": 35000000,
  "desiredJobType": "FULL_TIME",
  "desiredLocation": "TP. Hồ Chí Minh",
  "availability": "Có thể bắt đầu ngay"
}
```

#### 4.4 **Tải lên CV**
```json
POST /api/profile/documents
Authorization: Bearer {token_ung_vien}
Content-Type: multipart/form-data

file: cv_tran_van_a.pdf
documentType: CV
description: CV cập nhật của Trần Văn A
```

#### 4.5 **Tìm kiếm việc làm**
```json
GET /api/jobs/search?keyword=java&location=TP.HCM&experienceLevel=SENIOR&jobType=FULL_TIME
```

#### 4.6 **Xem chi tiết việc làm**
```json
GET /api/jobs/{jobId}/public
```

#### 4.7 **Nộp đơn ứng tuyển**
```json
POST /api/applications/my
Authorization: Bearer {token_ung_vien}
{
  "jobPostingId": {jobId},
  "coverLetter": "Kính gửi Ban tuyển dụng,\n\nTôi rất quan tâm đến vị trí Lập trình viên Java Senior tại Công ty Công nghệ TechCorp. Với 6 năm kinh nghiệm phát triển phần mềm Java và thành thạo các công nghệ Spring Boot, microservices, tôi tin rằng mình phù hợp với yêu cầu công việc.\n\nTrong quá trình làm việc, tôi đã có kinh nghiệm:\n- Phát triển các ứng dụng Java enterprise với Spring Boot\n- Thiết kế và triển khai kiến trúc microservices\n- Làm việc với Docker, Kubernetes\n- Tối ưu hóa hiệu suất database MySQL/PostgreSQL\n- Giao tiếp tiếng Anh tốt (TOEIC 750)\n\nTôi mong muốn được đóng góp vào sự phát triển của công ty và học hỏi thêm các công nghệ mới. Cảm ơn quý công ty đã xem xét hồ sơ của tôi.",
  "resumeUrl": "/uploads/resumes/{userId}/cv_tran_van_a.pdf",
  "additionalDocuments": ["chung_chi_java.pdf", "chung_chi_aws.pdf", "portfolio.pdf"]
}
```

---

### **PHẦN 5: NHÀ TUYỂN DỤNG XEM XÉT (3 phút)**

#### 5.1 **Xem đơn ứng tuyển cho job mới**
```json
GET /api/applications/job/{jobId}
Authorization: Bearer {token_nha_tuyen_dung}
```

**Kết quả**: Có 1 application mới cho job vừa tạo:
- Application ID: {new_application_id}
- Applicant: Trần Văn A
- Status: RECEIVED
- Cover Letter: "Kính gửi Ban tuyển dụng, Tôi rất quan tâm đến vị trí Lập trình viên Java Senior..."
- Resume URL: "/uploads/resumes/{userId}/cv_tran_van_a.pdf"
- Additional Documents: "chung_chi_java.pdf, chung_chi_aws.pdf, portfolio.pdf"

#### 5.2 **Xem chi tiết application**
```json
GET /api/applications/{applicationId}
Authorization: Bearer {token_nha_tuyen_dung}
```

**Kết quả**: Thông tin chi tiết ứng viên:
- Họ tên: Trần Văn A
- Email: ungvien.moi@example.com
- Phone: +84 901 234 567
- Bio: "Lập trình viên Java có kinh nghiệm 6 năm..."
- Skills: Java, Spring Boot, Microservices, Docker, Kubernetes
- Experience: 6 năm
- Education: Cử nhân Khoa học Máy tính - Đại học Bách Khoa TP.HCM

#### 5.3 **Cập nhật trạng thái application thành REVIEWED**
```json
PUT /api/applications/{applicationId}/status
Authorization: Bearer {token_nha_tuyen_dung}
{
  "status": "REVIEWED",
  "notes": "Hồ sơ ứng viên rất tốt, có kinh nghiệm phù hợp với yêu cầu. Kỹ năng Java và Spring Boot mạnh, có chứng chỉ Oracle và AWS. Đề xuất mời phỏng vấn."
}
```

#### 5.4 **Lên lịch phỏng vấn**
```json
POST /api/interviews
Authorization: Bearer {token_nha_tuyen_dung}
{
  "applicationId": {applicationId},
  "interviewDate": "2024-01-20T14:00:00",
  "interviewType": "TECHNICAL",
  "location": "Văn phòng TechCorp, 123 Nguyễn Huệ, Quận 1, TP.HCM",
  "notes": "Phỏng vấn kỹ thuật tập trung vào Java, Spring Boot và kiến trúc microservices. Sẽ có bài test coding và thảo luận về kinh nghiệm dự án.",
  "participants": ["nguyenb2110051@student.ctu.edu.vn", "tech.lead@techcorp.vn"]
}
```

#### 5.5 **Cập nhật trạng thái application thành INTERVIEW**
```json
PUT /api/applications/{applicationId}/status
Authorization: Bearer {token_nha_tuyen_dung}
{
  "status": "INTERVIEW",
  "notes": "Đã lên lịch phỏng vấn kỹ thuật vào 20/01/2024 lúc 14:00"
}
```

---

### **PHẦN 6: QUY TRÌNH PHỎNG VẤN (2 phút)**

#### 6.1 **Xem chi tiết phỏng vấn mới**
```json
GET /api/interviews/{interviewId}
Authorization: Bearer {token_nha_tuyen_dung}
```

**Kết quả**: Interview Schedule mới:
- Application ID: {application_id}
- Start Time: 2024-01-20 14:00:00
- End Time: 2024-01-20 15:00:00
- Method: OFFLINE
- Location: Văn phòng TechCorp, 123 Nguyễn Huệ, Quận 1, TP.HCM
- Status: SCHEDULED
- Note: "Phỏng vấn kỹ thuật tập trung vào Java, Spring Boot và kiến trúc microservices"

#### 6.2 **Cập nhật trạng thái phỏng vấn thành COMPLETED**
```json
PUT /api/interviews/{interviewId}/status
Authorization: Bearer {token_nha_tuyen_dung}
{
  "status": "COMPLETED",
  "notes": "Phỏng vấn hoàn tất. Ứng viên thể hiện tốt:\n- Kỹ năng Java và Spring Boot mạnh\n- Hiểu rõ kiến trúc microservices\n- Có kinh nghiệm với Docker, Kubernetes\n- Giao tiếp tiếng Anh tốt\n- Đề xuất tuyển dụng với mức lương 32M VND"
}
```

#### 6.3 **Cập nhật trạng thái application thành OFFER**
```json
PUT /api/applications/{applicationId}/status
Authorization: Bearer {token_nha_tuyen_dung}
{
  "status": "OFFER",
  "notes": "Chúc mừng! Chúng tôi muốn mời bạn tham gia đội ngũ TechCorp với vị trí Lập trình viên Java Senior. Mức lương: 32,000,000 VND/tháng + thưởng KPI. Ngày bắt đầu: 01/02/2024"
}
```

#### 6.4 **Cập nhật trạng thái cuối cùng thành HIRED**
```json
PUT /api/applications/{applicationId}/status
Authorization: Bearer {token_nha_tuyen_dung}
{
  "status": "HIRED",
  "notes": "Chào mừng Trần Văn A đến với đội ngũ TechCorp! Chúng tôi rất vui được chào đón bạn. Hẹn gặp bạn vào ngày 01/02/2024 tại văn phòng."
}
```

#### 6.5 **Xem thông báo cho applicant**
```json
GET /api/notifications
Authorization: Bearer {token_ung_vien}
```

**Kết quả**: Applicant sẽ nhận được notifications:
- "Bạn có lịch phỏng vấn mới" - Thời gian: 20/01/2024 14:00
- "Phỏng vấn đã hoàn tất" - Phỏng vấn xong, chờ đánh giá
- "Chúc mừng! Bạn đã được tuyển dụng" - Chào mừng đến với TechCorp

---

## 📊 DEMO CHECKPOINTS - LUỒNG MỚI

### **✅ Admin Checkpoints**
- [ ] Admin dashboard hiển thị thống kê: 7 users (tăng 1), 5 companies, 5 jobs (tăng 1), 3 applications (tăng 1)
- [ ] Có thể xem tất cả users, jobs, applications
- [ ] Theo dõi user mới: Trần Văn A (APPLICANT)
- [ ] Quản lý refresh tokens

### **✅ Employer Checkpoints (nguyenb2110051@student.ctu.edu.vn)**
- [ ] Company profile đã cập nhật: Công ty Công nghệ TechCorp
- [ ] 1 job posting mới đã tạo và ACTIVE: "Lập trình viên Java Senior"
- [ ] 1 application mới từ ứng viên Trần Văn A
- [ ] Application status workflow hoàn chỉnh: RECEIVED → REVIEWED → INTERVIEW → OFFER → HIRED
- [ ] 1 interview schedule mới đã được tạo
- [ ] Timeline tracking đầy đủ cho application

### **✅ Applicant Checkpoints (Trần Văn A - MỚI)**
- [ ] Tài khoản mới được tạo và verify email
- [ ] Profile đầy đủ: Bio, Skills, Experience, Education, Certifications
- [ ] CV đã upload: `/uploads/resumes/{userId}/cv_tran_van_a.pdf`
- [ ] 1 application mới đã nộp với cover letter chi tiết
- [ ] Nhận được notifications về phỏng vấn và kết quả tuyển dụng
- [ ] Timeline tracking cho application từ RECEIVED đến HIRED

## 🗄️ THỐNG KÊ CƠ SỞ DỮ LIỆU - SAU LUỒNG MỚI

### **Bảng chính và số lượng bản ghi (sau demo)**:
- **users**: 7 người dùng (1 Admin, 2 Employer, 1 Recruiter, 3 Applicant)
- **companies**: 5 công ty (Tech Innovate Co., Digital Solutions Ltd., StartUp Hub, Tech Company Ltd, Công ty Công nghệ TechCorp)
- **job_postings**: 5 tin tuyển dụng (3 ACTIVE, 2 khác)
- **applications**: 3 đơn ứng tuyển (1 mới từ Trần Văn A)
- **application_timelines**: 15+ timeline entries (tăng 4 entries mới)
- **interview_schedules**: 3 lịch phỏng vấn (1 mới)
- **profiles**: 3 hồ sơ ứng viên (1 mới)
- **notifications**: 5+ thông báo (tăng 3 notifications mới)
- **refresh_tokens**: 75+ tokens (tăng 4 tokens mới)
- **skills**: 8 kỹ năng (Java, Spring Boot, React, MySQL, JavaScript, Python, Docker, Kubernetes)

### **Dữ liệu mới được tạo trong demo**:
- **User mới**: Trần Văn A (ungvien.moi@example.com) - APPLICANT
- **Company cập nhật**: Công ty Công nghệ TechCorp (từ Tech Company Ltd)
- **Job mới**: "Lập trình viên Java Senior" - ACTIVE, salary 25-40M VND
- **Application mới**: Trần Văn A apply job mới với cover letter chi tiết
- **Interview mới**: Phỏng vấn kỹ thuật ngày 20/01/2024
- **Profile mới**: Trần Văn A có profile đầy đủ với skills, certifications
- **Timeline mới**: RECEIVED → REVIEWED → INTERVIEW → OFFER → HIRED
- **Notifications mới**: 3 thông báo cho ứng viên mới

---

## 🎯 DEMO TALKING POINTS

### **1. Authentication & Security**
- "Hệ thống sử dụng JWT authentication với role-based access control"
- "Email verification bắt buộc trước khi login"
- "Rate limiting để bảo vệ API"

### **2. Job Management**
- "Employer có thể tạo job với đầy đủ thông tin"
- "Job status workflow: DRAFT → ACTIVE → CLOSED"
- "Advanced search với multiple filters"

### **3. Application Process**
- "Applicant có thể nộp đơn với cover letter và documents"
- "Application status workflow: RECEIVED → REVIEWED → INTERVIEW → HIRED/REJECTED"
- "Real-time status tracking"

### **4. Interview Management**
- "Schedule interview với conflict checking"
- "Email notifications với .ics attachment"
- "Interview status tracking"

### **5. Analytics & Reporting**
- "Admin dashboard với system overview"
- "Employer dashboard với hiring metrics"
- "Real-time data với caching"

---

## 🛠️ TECHNICAL HIGHLIGHTS

### **Backend Features**
- Spring Boot với JPA/Hibernate
- JWT Authentication & Authorization
- File upload với validation
- Email service với templates
- Audit logging
- Rate limiting
- CORS configuration

### **Database Features**
- Relational database design
- Foreign key constraints
- Indexing cho performance
- Data validation

### **API Features**
- RESTful API design
- Consistent error handling
- Pagination support
- Search & filtering
- File upload/download

---

## 🚨 TROUBLESHOOTING

### **Common Issues**
1. **JAVA_HOME not set**: Set JAVA_HOME environment variable
2. **Port 8081 in use**: Change port in application.properties
3. **Database connection**: Check MySQL service running
4. **Email not sending**: Check SMTP configuration

### **Debug Commands**
```bash
# Check server status
curl http://localhost:8081/api/health

# Check database connection
mysql -u root -p recruitment_db

# View logs
tail -f logs/application.log
```

---

## 📝 POST-DEMO Q&A

### **Potential Questions**
1. **Scalability**: "How does the system handle high traffic?"
2. **Security**: "What security measures are implemented?"
3. **Integration**: "Can it integrate with other HR systems?"
4. **Mobile**: "Is there a mobile app version?"
5. **Analytics**: "What reporting capabilities are available?"

### **Answers**
1. **Scalability**: "Spring Boot với connection pooling, caching, và database optimization"
2. **Security**: "JWT tokens, input sanitization, rate limiting, audit logging"
3. **Integration**: "RESTful API có thể integrate với bất kỳ system nào"
4. **Mobile**: "API-first design, có thể build mobile app dễ dàng"
5. **Analytics**: "Real-time dashboard với metrics và reporting"

---

## 🎉 DEMO CONCLUSION - LUỒNG MỚI

**Tóm tắt**: Demo hoàn chỉnh luồng tuyển dụng mới với:
- **Admin**: Quản lý hệ thống, theo dõi thống kê
- **Employer (nguyenb2110051@student.ctu.edu.vn)**: Tạo job mới, xem xét ứng viên, phỏng vấn, tuyển dụng
- **Applicant mới (Trần Văn A)**: Đăng ký, tạo profile, nộp đơn, phỏng vấn, được tuyển

**Workflow hoàn chỉnh**: 
1. Employer tạo job → Applicant nộp đơn → Employer review → Lên lịch phỏng vấn → Phỏng vấn → Offer → Hired

**Dữ liệu mới**: 1 user mới, 1 job mới, 1 application mới, 1 interview mới, 3 notifications mới

**Next Steps**: 
- Deploy to production
- Add mobile app
- Integrate with external systems
- Advanced analytics
- Multi-language support
