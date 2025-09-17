#!/usr/bin/env pwsh

# Test API nhanh cho Recruitment System
Write-Host "🚀 TESTING RECRUITMENT SYSTEM API" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# Kiểm tra server
Write-Host "`n1. 🔍 Kiểm tra server..." -ForegroundColor Yellow
try {
    $debugResponse = Invoke-RestMethod -Uri "http://localhost:8081/debug/encode?password=test123" -Method GET -TimeoutSec 5
    Write-Host "✅ Server đang chạy!" -ForegroundColor Green
    Write-Host "   Debug response: $debugResponse" -ForegroundColor Gray
} catch {
    Write-Host "❌ Server không chạy! Lỗi: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "   Hãy chạy server trước khi test API" -ForegroundColor Yellow
    exit 1
}

# Test đăng ký user mới
Write-Host "`n2. 📝 Test đăng ký user mới..." -ForegroundColor Yellow
$timestamp = Get-Date -Format "yyyyMMddHHmmss"
$testEmail = "testuser$timestamp@example.com"

$registerData = @{
    email = $testEmail
    password = "test123"
    fullName = "Test User $timestamp"
    phone = "01234567$($timestamp.Substring(-2))"
    role = "APPLICANT"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method POST -Body $registerData -ContentType "application/json" -TimeoutSec 10
    Write-Host "✅ Đăng ký thành công!" -ForegroundColor Green
    Write-Host "   Email: $testEmail" -ForegroundColor Gray
    Write-Host "   User ID: $($registerResponse.user.id)" -ForegroundColor Gray
    Write-Host "   Token (first 50 chars): $($registerResponse.token.Substring(0, [Math]::Min(50, $registerResponse.token.Length)))..." -ForegroundColor Gray
    
    $registrationSuccess = $true
} catch {
    Write-Host "❌ Đăng ký thất bại!" -ForegroundColor Red
    Write-Host "   Lỗi: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody" -ForegroundColor Red
    }
    $registrationSuccess = $false
}

# Test đăng nhập với user vừa tạo
if ($registrationSuccess) {
    Write-Host "`n3. 🔐 Test đăng nhập với user vừa tạo..." -ForegroundColor Yellow
    
    $loginData = @{
        email = $testEmail
        password = "test123"
    } | ConvertTo-Json
    
    try {
        $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method POST -Body $loginData -ContentType "application/json" -TimeoutSec 10
        Write-Host "✅ Đăng nhập user mới thành công!" -ForegroundColor Green
        Write-Host "   Token (first 50 chars): $($loginResponse.token.Substring(0, [Math]::Min(50, $loginResponse.token.Length)))..." -ForegroundColor Gray
    } catch {
        Write-Host "❌ Đăng nhập user mới thất bại!" -ForegroundColor Red
        Write-Host "   Lỗi: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
            $responseBody = $reader.ReadToEnd()
            Write-Host "   Response: $responseBody" -ForegroundColor Red
        }
    }
}

# Test đăng nhập với user có sẵn
Write-Host "`n4. 🔐 Test đăng nhập với user có sẵn..." -ForegroundColor Yellow

$existingLoginData = @{
    email = "testuser@example.com"
    password = "applicant123"
} | ConvertTo-Json

try {
    $existingLoginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method POST -Body $existingLoginData -ContentType "application/json" -TimeoutSec 10
    Write-Host "✅ Đăng nhập user có sẵn thành công!" -ForegroundColor Green
    Write-Host "   Token (first 50 chars): $($existingLoginResponse.token.Substring(0, [Math]::Min(50, $existingLoginResponse.token.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ Đăng nhập user có sẵn thất bại!" -ForegroundColor Red
    Write-Host "   Lỗi: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $responseBody = $reader.ReadToEnd()
        Write-Host "   Response: $responseBody" -ForegroundColor Red
    }
}

# Debug password encoding
Write-Host "`n5. 🔍 Debug password encoding..." -ForegroundColor Yellow
try {
    $encodedPassword = Invoke-RestMethod -Uri "http://localhost:8081/debug/encode?password=applicant123" -Method GET -TimeoutSec 5
    Write-Host "✅ Encoded 'applicant123': $encodedPassword" -ForegroundColor Green
    
    $verifyResponse = Invoke-RestMethod -Uri "http://localhost:8081/debug/verify?password=applicant123&encoded=$([System.Web.HttpUtility]::UrlEncode($encodedPassword))" -Method GET -TimeoutSec 5
    Write-Host "✅ Verify result: $verifyResponse" -ForegroundColor Green
} catch {
    Write-Host "❌ Debug encoding thất bại: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n🎯 KẾT QUẢ TEST HOÀN THÀNH!" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan
Write-Host "Nếu có lỗi, kiểm tra:" -ForegroundColor Yellow
Write-Host "1. Server có đang chạy không (http://localhost:8081)" -ForegroundColor Gray
Write-Host "2. MySQL có đang chạy không (XAMPP)" -ForegroundColor Gray
Write-Host "3. Database có dữ liệu không" -ForegroundColor Gray