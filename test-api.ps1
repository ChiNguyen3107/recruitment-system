# Test API Script
Write-Host "Testing Login API..."

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/login" -Method POST -ContentType "application/json" -Body '{"email":"applicant@test.com","password":"applicant123"}'
    Write-Host "Login Success!" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json -Depth 3)"
    
    # Test authenticated endpoint
    $headers = @{
        "Authorization" = "Bearer $($response.token)"
        "Content-Type" = "application/json"
    }
    
    Write-Host "`nTesting authenticated endpoint..."
    $userInfo = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/me" -Method GET -Headers $headers
    Write-Host "User Info: $($userInfo | ConvertTo-Json -Depth 3)" -ForegroundColor Cyan
    
} catch {
    Write-Host "API Test Failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Error Details: $($_.ErrorDetails.Message)" -ForegroundColor Yellow
}