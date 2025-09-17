#!/usr/bin/env pwsh

# Script chạy Recruitment System
Write-Host "🚀 ĐANG KHỞI ĐỘNG RECRUITMENT SYSTEM..." -ForegroundColor Green

# Thiết lập JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17.0.1"
Write-Host "✅ Đã thiết lập JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Yellow

# Chuyển vào thư mục dự án
$projectPath = "D:\xampp\htdocs\HTQL_TuyenDung\recruitment-system"
Set-Location $projectPath
Write-Host "✅ Đã chuyển vào thư mục: $projectPath" -ForegroundColor Yellow

# Kiểm tra MySQL
$mysqlProcess = Get-Process mysqld -ErrorAction SilentlyContinue
if ($mysqlProcess) {
    Write-Host "✅ MySQL đang chạy (PID: $($mysqlProcess.Id))" -ForegroundColor Green
} else {
    Write-Host "❌ MySQL không chạy! Hãy khởi động XAMPP trước." -ForegroundColor Red
    exit 1
}

# Kiểm tra port 8081
$portCheck = netstat -ano | Select-String ":8081"
if ($portCheck) {
    Write-Host "⚠️  Port 8081 đang được sử dụng:" -ForegroundColor Yellow
    Write-Host $portCheck -ForegroundColor Red
    $choice = Read-Host "Bạn có muốn tiếp tục? (y/N)"
    if ($choice -ne "y") {
        exit 1
    }
}

# Kiểm tra file JAR
$jarFile = "target\recruitment-system-1.0.0.jar"
if (Test-Path $jarFile) {
    Write-Host "✅ Tìm thấy file JAR: $jarFile" -ForegroundColor Green
} else {
    Write-Host "❌ Không tìm thấy file JAR. Đang build..." -ForegroundColor Yellow
    
    # Build project
    Write-Host "🔨 Đang build dự án..." -ForegroundColor Cyan
    & mvn clean package -DskipTests
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build thành công!" -ForegroundColor Green
    } else {
        Write-Host "❌ Build thất bại!" -ForegroundColor Red
        exit 1
    }
}

# Chạy ứng dụng
Write-Host "🎯 Đang khởi động server trên port 8081..." -ForegroundColor Cyan
Write-Host "📱 URL: http://localhost:8081" -ForegroundColor Magenta
Write-Host "🛑 Nhấn Ctrl+C để dừng server" -ForegroundColor Yellow
Write-Host ""

try {
    # Chạy với output đầy đủ
    java -jar $jarFile
} catch {
    Write-Host "❌ Lỗi khi chạy server: $_" -ForegroundColor Red
} finally {
    Write-Host "🔴 Server đã dừng." -ForegroundColor Red
}