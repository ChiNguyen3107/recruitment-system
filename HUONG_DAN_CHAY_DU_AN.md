# HƯỚNG DẪN CHẠY DỰ ÁN RECRUITMENT SYSTEM

## 🎯 TỔNG QUAN DỰ ÁN
- **Tên dự án:** Hệ thống quản lý tuyển dụng
- **Công nghệ:** Spring Boot 3.2.0, Java 17, MySQL, JWT Authentication
- **Port:** 8081
- **Database:** recruitment_db

## ✅ YÊU CẦU HỆ THỐNG

### Phần mềm cần thiết:
- ☕ **Java JDK 17+** (Oracle JDK, OpenJDK, hoặc Eclipse Temurin)
- 🗄️ **MySQL Server** (XAMPP, MySQL Workbench, hoặc standalone MySQL)
- 🌐 **Postman** (tùy chọn, để test API)

### Kiểm tra cài đặt:
```powershell
# Kiểm tra Java version (phải có 17 trở lên)
java -version
# Kết quả mong đợi: java version "17.x.x" hoặc cao hơn

# Kiểm tra MySQL đang chạy
netstat -an | findstr :3306
# Kết quả mong đợi: TCP 0.0.0.0:3306 LISTENING
```

### 📁 Tìm đường dẫn Java trên máy bạn:
```powershell
# Windows - Tìm tất cả JDK đã cài
Get-ChildItem "C:\Program Files\Java\" -Directory | Where-Object {$_.Name -like "*jdk*"}
Get-ChildItem "C:\Program Files (x86)\Java\" -Directory | Where-Object {$_.Name -like "*jdk*"}

# Hoặc kiểm tra biến môi trường hiện tại
echo $env:JAVA_HOME
```

## 🚀 CÁCH CHẠY DỰ ÁN

### 🔥 CÁCH NHANH NHẤT (Maven Wrapper)

1. **Mở PowerShell/Terminal** tại thư mục dự án:
   ```powershell
   # Điều hướng đến thư mục dự án của bạn
   cd "ĐƯỜNG_DẪN_ĐẾN_THỦ_MỤC_DỰ_ÁN"
   # Ví dụ: cd "D:\xampp\htdocs\recruitment-system"
   # Hoặc: cd "C:\Users\YourName\Documents\recruitment-system"
   ```

2. **Thiết lập JAVA_HOME và chạy:**
   ```powershell
   # Thay thế ĐƯỜNG_DẪN_JAVA bằng đường dẫn JDK trên máy bạn
   $env:JAVA_HOME = "ĐƯỜNG_DẪN_JAVA"
   
   # Ví dụ các đường dẫn Java phổ biến:
   # $env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.1"
   # $env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.1"  
   # $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.2.8-hotspot"
   
   # Chạy ứng dụng
   ./mvnw.cmd spring-boot:run
   ```

3. **Đợi thông báo khởi động thành công:**
   ```
   Tomcat started on port 8081 (http)
   Started RecruitmentSystemApplication
   ```

### 🎯 CÁCH KHÁC

#### Cách 1: Chạy bằng JAR file có sẵn
```powershell
# Chạy trực tiếp JAR file
java -jar target/recruitment-system-1.0.0.jar
```

#### Cách 2: Chạy bằng IDE (VS Code/IntelliJ)
1. Mở thư mục `recruitment-system` trong IDE
2. Tìm file `RecruitmentSystemApplication.java` trong `src/main/java/com/recruitment/system/`
3. Click chuột phải → Run/Debug

#### Cách 3: Build lại và chạy
```powershell
# Build dự án mới
./mvnw.cmd clean package

# Chạy JAR file vừa build
java -jar target/recruitment-system-1.0.0.jar
```

## 🔍 KIỂM TRA SERVER HOẠT ĐỘNG

### Bước 1: Xác nhận server đã khởi động
- 🌐 **URL:** http://localhost:8081
- 📋 **Log thành công:** `Tomcat started on port 8081`
- ⏱️ **Thời gian khởi động:** 30-60 giây

### Bước 2: Test endpoints cơ bản
```powershell
# Test server có hoạt động không
Invoke-RestMethod -Uri "http://localhost:8081/debug/encode?password=test123" -Method GET

# Kết quả mong đợi: Trả về password đã được mã hóa
```

## 🗄️ THIẾT LẬP DATABASE

### Kiểm tra MySQL đang chạy:
```powershell
# Cách 1: Kiểm tra port
netstat -an | findstr :3306

# Cách 2: Kiểm tra service
Get-Service | Where-Object {$_.Name -like "*mysql*"}
```

### Kiểm tra database tồn tại:
```powershell
# Tìm đường dẫn MySQL trên máy bạn:
# XAMPP: thường tại C:\xampp\mysql\bin\mysql.exe
# MySQL Workbench: C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
# Hoặc sử dụng mysql command nếu đã có trong PATH

# Cách 1: Nếu dùng XAMPP
& "C:\xampp\mysql\bin\mysql.exe" -u root -e "SHOW DATABASES LIKE 'recruitment_db';"

# Cách 2: Nếu MySQL đã có trong PATH
mysql -u root -e "SHOW DATABASES LIKE 'recruitment_db';"

# Cách 3: Nếu cài MySQL standalone
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -e "SHOW DATABASES LIKE 'recruitment_db';"
```

### Nếu chưa có database, chạy import:
```powershell
# Tạo database (thay đổi đường dẫn mysql.exe phù hợp với máy bạn)
& "ĐƯỜNG_DẪN_MYSQL\mysql.exe" -u root -e "CREATE DATABASE IF NOT EXISTS recruitment_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# Import schema
& "ĐƯỜNG_DẪN_MYSQL\mysql.exe" -u root recruitment_db < database_schema.sql

# Ví dụ với XAMPP:
# & "C:\xampp\mysql\bin\mysql.exe" -u root recruitment_db < database_schema.sql
```

## 🔧 KHẮC PHỤC SỰ CỐ THƯỜNG GẶP

### ❌ Vấn đề 1: "JAVA_HOME not found"
**Nguyên nhân:** Biến môi trường JAVA_HOME chưa được thiết lập

**Giải pháp:**
```powershell
# Cách 1: Thiết lập JAVA_HOME tạm thời trong PowerShell
# Tìm đường dẫn Java trên máy bạn trước:
Get-ChildItem "C:\Program Files\Java\" -Directory | Where-Object {$_.Name -like "*jdk*"}

# Sau đó thiết lập (thay đổi đường dẫn phù hợp):
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.x"  # Thay x bằng version thực tế
# Hoặc
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"
# Hoặc
$env:JAVA_HOME = "C:\Program Files\OpenJDK\openjdk-17.x.x"

# Cách 2: Thiết lập vĩnh viễn (khuyến nghị)
# Control Panel → System → Advanced System Settings → Environment Variables
# Thêm biến mới: JAVA_HOME = đường_dẫn_jdk_của_bạn
```

### ❌ Vấn đề 2: Port 8081 đã được sử dụng
**Triệu chứng:** `Port 8081 was already in use`

**Giải pháp:**
```powershell
# Tìm process đang sử dụng port 8081
netstat -ano | findstr :8081

# Kill process (thay PID_NUMBER bằng số PID thực)
taskkill /PID [PID_NUMBER] /F

# Hoặc thay đổi port trong application.properties
# server.port=8082
```

### ❌ Vấn đề 3: Lỗi kết nối database
**Triệu chứng:** `Cannot connect to database` hoặc `Connection refused`

**Giải pháp:**
1. **Khởi động MySQL service:**
   
   **Nếu dùng XAMPP:**
   - Mở XAMPP Control Panel
   - Click "Start" cho MySQL service
   - Đợi status thành "Running" (màu xanh)
   
   **Nếu dùng MySQL Workbench/Standalone:**
   - Mở Services (services.msc)
   - Tìm "MySQL" service
   - Click "Start" nếu chưa chạy
   
   **Nếu dùng Command Line:**
   ```powershell
   # Windows Service
   net start mysql80  # hoặc mysql57, mysql tùy version
   ```

2. **Kiểm tra kết nối:**
   ```powershell
   # Thay đổi đường dẫn mysql phù hợp với cài đặt của bạn
   & "ĐƯỜNG_DẪN_MYSQL\mysql.exe" -u root -e "SELECT 1;"
   
   # Ví dụ:
   # & "C:\xampp\mysql\bin\mysql.exe" -u root -e "SELECT 1;"
   # mysql -u root -e "SELECT 1;"  # Nếu MySQL trong PATH
   ```

3. **Kiểm tra database tồn tại:**
   ```powershell
   & "ĐƯỜNG_DẪN_MYSQL\mysql.exe" -u root -e "SHOW DATABASES LIKE 'recruitment_db';"
   ```

### ❌ Vấn đề 4: Maven build failed
**Triệu chứng:** Build failure khi chạy mvnw

**Giải pháp:**
```powershell
# Clean cache và rebuild
./mvnw.cmd clean
./mvnw.cmd clean compile
./mvnw.cmd clean package

# Nếu vẫn lỗi, xóa thư mục target và build lại
Remove-Item -Recurse -Force target
./mvnw.cmd clean package
```

### ❌ Vấn đề 5: Server khởi động rồi tự tắt
**Nguyên nhân:** Lỗi trong code hoặc cấu hình

**Giải pháp:**
1. **Xem log chi tiết:**
   ```powershell
   ./mvnw.cmd spring-boot:run -X
   ```

2. **Kiểm tra các dependency conflict:**
   ```powershell
   ./mvnw.cmd dependency:tree
   ```

## 🧪 TEST API VÀ CHỨC NĂNG

### 🌐 Thông tin API cơ bản
- **Base URL:** http://localhost:8081
- **API Authentication:** JWT Token
- **Content-Type:** application/json

### 🔑 Endpoints quan trọng
| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/api/auth/register` | POST | Đăng ký tài khoản mới |
| `/api/auth/login` | POST | Đăng nhập |
| `/debug/encode` | GET | Mã hóa password (debug) |
| `/api/users/profile` | GET | Xem thông tin profile |
| `/api/jobs` | GET | Danh sách việc làm |

### 🧪 Test nhanh bằng PowerShell

#### Test 1: Kiểm tra server hoạt động
```powershell
try {
    $result = Invoke-RestMethod -Uri "http://localhost:8081/debug/encode?password=test123" -Method GET
    Write-Host "✅ Server đang hoạt động. Encoded password: $result" -ForegroundColor Green
} catch {
    Write-Host "❌ Server không hoạt động: $($_.Exception.Message)" -ForegroundColor Red
}
```

#### Test 2: Đăng ký tài khoản mới
```powershell
$registerData = @{
    email = "newuser@example.com"
    password = "password123"
    firstName = "Test"
    lastName = "User"
    phoneNumber = "0123456789"
    role = "APPLICANT"
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method POST -Body $registerData -ContentType "application/json"
    Write-Host "✅ Đăng ký thành công!" -ForegroundColor Green
    Write-Host "User ID: $($result.user.id)"
} catch {
    Write-Host "❌ Đăng ký thất bại: $($_.Exception.Message)" -ForegroundColor Red
}
```

#### Test 3: Đăng nhập
```powershell
$loginData = @{
    email = "admin@recruitment.com"
    password = "admin123"
} | ConvertTo-Json

try {
    $result = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method POST -Body $loginData -ContentType "application/json"
    Write-Host "✅ Đăng nhập thành công!" -ForegroundColor Green
    Write-Host "Token: $($result.token)"
    
    # Lưu token để sử dụng cho các request khác
    $global:jwt_token = $result.token
} catch {
    Write-Host "❌ Đăng nhập thất bại: $($_.Exception.Message)" -ForegroundColor Red
}
```

### 👥 Tài khoản test có sẵn trong database
| Role | Email | Password | Mô tả |
|------|-------|----------|-------|
| ADMIN | admin@recruitment.com | admin123 | Quản trị viên |
| EMPLOYER | employer@techinnovate.com | employer123 | Nhà tuyển dụng |
| APPLICANT | applicant@test.com | applicant123 | Ứng viên |
## 📱 SỬ DỤNG POSTMAN (TÙY CHỌN)

### 📥 Import Collection và Environment
1. **Mở Postman**
2. **Import Collection:**
   - File → Import
   - Chọn file: `Recruitment_System_API.postman_collection.json`
   
3. **Import Environment:**
   - File → Import  
   - Chọn file: `recruitment-system-environment.json`
   
4. **Chọn Environment:**
   - Góc phải trên: Chọn "Recruitment System"

### 🧪 Test Cases có sẵn trong Postman
1. **🔐 Auth Tests:**
   - Register New User
   - Login Admin
   - Login Employer  
   - Login Applicant

2. **👤 User Management:**
   - Get User Profile
   - Update Profile
   - Change Password

3. **💼 Job Management:**
   - Get All Jobs
   - Create Job (Employer only)
   - Update Job
   - Delete Job

### 🔑 Sử dụng JWT Token
Sau khi đăng nhập thành công, token sẽ tự động được lưu vào Environment variable `jwt_token` và sử dụng cho các request tiếp theo.

## � SCRIPT TỰ ĐỘNG

### � Script khởi động nhanh (PowerShell)
Tạo file `start-project.ps1` trong thư mục dự án:
```powershell
# start-project.ps1
Write-Host "🚀 Đang khởi động Recruitment System..." -ForegroundColor Cyan

# Kiểm tra Java
Write-Host "📋 Kiểm tra Java..." -ForegroundColor Yellow
try {
    $javaVersion = java -version 2>&1 | Select-String "version"
    Write-Host "✅ Java OK: $javaVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Java không tìm thấy!" -ForegroundColor Red
    Write-Host "💡 Hãy cài đặt Java 17+ và thêm vào PATH" -ForegroundColor Yellow
    exit 1
}

# Kiểm tra MySQL
Write-Host "📋 Kiểm tra MySQL..." -ForegroundColor Yellow
$mysqlPort = netstat -an | findstr :3306
if ($mysqlPort) {
    Write-Host "✅ MySQL đang chạy trên port 3306" -ForegroundColor Green
} else {
    Write-Host "❌ MySQL không chạy!" -ForegroundColor Red
    Write-Host "💡 Hãy khởi động MySQL service (XAMPP/MySQL Workbench/Services)" -ForegroundColor Yellow
    exit 1
}

# Tự động tìm JAVA_HOME
Write-Host "🔧 Tìm JAVA_HOME..." -ForegroundColor Yellow
$javaHomes = @(
    "C:\Program Files\Java\jdk-17*",
    "C:\Program Files\Java\jdk-21*", 
    "C:\Program Files\Eclipse Adoptium\jdk-17*",
    "C:\Program Files\OpenJDK\openjdk-17*"
)

$foundJava = $false
foreach ($path in $javaHomes) {
    $javaDir = Get-ChildItem $path -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($javaDir) {
        $env:JAVA_HOME = $javaDir.FullName
        Write-Host "✅ Tìm thấy JAVA_HOME: $($env:JAVA_HOME)" -ForegroundColor Green
        $foundJava = $true
        break
    }
}

if (-not $foundJava) {
    Write-Host "❌ Không tìm thấy JAVA_HOME tự động!" -ForegroundColor Red
    Write-Host "💡 Hãy thiết lập thủ công: `$env:JAVA_HOME = 'ĐƯỜNG_DẪN_JDK'" -ForegroundColor Yellow
    exit 1
}

Write-Host "🚀 Khởi động ứng dụng..." -ForegroundColor Cyan
./mvnw.cmd spring-boot:run
```

### Sử dụng script:
```powershell
# Chạy quyền admin PowerShell nếu cần
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# Chạy script
.\start-project.ps1
```

## ⚠️ LƯU Ý QUAN TRỌNG

### 🔥 Trước khi khởi động:
1. **✅ MySQL service** phải đang chạy:
   - **XAMPP:** MySQL status = "Running" (màu xanh)
   - **MySQL Workbench:** Service đã start
   - **Standalone:** MySQL80 service đang chạy
2. **✅ Port 8081** phải trống (không có ứng dụng khác sử dụng)
3. **✅ Database recruitment_db** phải tồn tại và có dữ liệu
4. **✅ Java 17+** phải được cài đặt và JAVA_HOME được thiết lập

### 🔄 Khi thay đổi code:
```powershell
# Stop server hiện tại (Ctrl+C trong terminal)
# Sau đó rebuild:
./mvnw.cmd clean package

# Rồi chạy lại:
./mvnw.cmd spring-boot:run
```

### 🗂️ Cấu trúc thư mục quan trọng:
```
recruitment-system/               # Thư mục gốc của dự án
├── src/main/java/com/recruitment/system/
│   ├── RecruitmentSystemApplication.java  # Main class
│   ├── controller/                        # REST Controllers  
│   ├── service/                          # Business Logic
│   ├── repository/                       # Data Access
│   └── entity/                           # Database Models
├── src/main/resources/
│   └── application.properties            # Cấu hình app
├── target/
│   └── recruitment-system-1.0.0.jar     # File JAR build
├── database_schema.sql                   # Database schema
├── mvnw.cmd                             # Maven wrapper (Windows)
└── mvnw                                 # Maven wrapper (Linux/Mac)
```

### 🎯 Endpoint URLs để bookmark:
- **🏠 Base URL:** http://localhost:8081
- **🔐 Login API:** http://localhost:8081/api/auth/login
- **📝 Register API:** http://localhost:8081/api/auth/register
- **🛠️ Debug Encode:** http://localhost:8081/debug/encode?password=test123

## 🆘 SUPPORT VÀ DEBUG

### 📊 Kiểm tra trạng thái hệ thống:
```powershell
# Kiểm tra Java
java -version

# Kiểm tra MySQL service
Get-Service | Where-Object {$_.Name -like "*mysql*"}

# Kiểm tra port đang sử dụng
netstat -ano | findstr :8081
netstat -ano | findstr :3306

# Kiểm tra process Java đang chạy
Get-Process | Where-Object {$_.ProcessName -like "*java*"}
```

### 📋 Log và Debug:
```powershell
# Chạy với log chi tiết
./mvnw.cmd spring-boot:run -X

# Kiểm tra dependency conflicts
./mvnw.cmd dependency:tree

# Test kết nối database (thay đường dẫn mysql phù hợp)
& "ĐƯỜNG_DẪN_MYSQL\mysql.exe" -u root -e "SELECT VERSION();"

# Ví dụ các đường dẫn MySQL phổ biến:
# & "C:\xampp\mysql\bin\mysql.exe" -u root -e "SELECT VERSION();"
# & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -e "SELECT VERSION();"
# mysql -u root -e "SELECT VERSION();"  # Nếu MySQL có trong PATH
```

### 🔍 Các lỗi thường gặp và cách xử lý:
| Lỗi | Nguyên nhân | Giải pháp |
|-----|-------------|-----------|
| `JAVA_HOME not found` | Biến môi trường chưa set | Tìm JDK và set: `$env:JAVA_HOME = "ĐƯỜNG_DẪN_JDK"` |
| `Port 8081 already in use` | Port bị chiếm | `taskkill /PID [PID] /F` |
| `Connection refused` | MySQL chưa chạy | Khởi động MySQL service |
| `Database does not exist` | Database chưa import | Import database_schema.sql |
| `Build failure` | Cache Maven lỗi | `./mvnw.cmd clean` |
| `mvnw command not found` | Không có Maven wrapper | Dùng `mvn` hoặc cài Maven |

### 📞 Khi cần hỗ trợ:
Cung cấp thông tin sau:
1. **Hệ điều hành:** Windows 10/11, macOS, Linux
2. **Java version:** `java -version`
3. **MySQL type:** XAMPP, MySQL Workbench, Standalone, Docker
4. **JAVA_HOME:** `echo $env:JAVA_HOME` (Windows) hoặc `echo $JAVA_HOME` (Linux/Mac)
5. **Error message** đầy đủ từ console  
6. **Trạng thái MySQL:** Screenshot MySQL service status
7. **Port status:** `netstat -ano | findstr :8081`
8. **Project location:** Đường dẫn thư mục dự án

### 🌍 Hỗ trợ đa nền tảng:
```bash
# Linux/macOS
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk"  # hoặc đường dẫn phù hợp
./mvnw spring-boot:run

# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.x.x"
./mvnw.cmd spring-boot:run

# Windows Command Prompt
set JAVA_HOME=C:\Program Files\Java\jdk-17.x.x
mvnw.cmd spring-boot:run
```

---

## 🎉 HOÀN THÀNH!

Nếu làm theo hướng dẫn trên, bạn sẽ có:
- ✅ Server chạy tại http://localhost:8081
- ✅ Database kết nối thành công
- ✅ API endpoints hoạt động
- ✅ JWT authentication đã setup
- ✅ Tài khoản test sẵn sàng sử dụng

**🚀 Chúc bạn code vui vẻ!**