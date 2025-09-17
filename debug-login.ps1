# Debug Login API
Write-Host "Testing Login API with detailed debugging..." -ForegroundColor Yellow

$loginData = @{
    email = "testuser@example.com"
    password = "password123"
}

$json = $loginData | ConvertTo-Json
Write-Host "Request JSON: $json" -ForegroundColor Cyan

try {
    $headers = @{
        'Content-Type' = 'application/json'
        'Accept' = 'application/json'
    }
    
    Write-Host "Sending request to: http://localhost:8081/api/auth/login" -ForegroundColor Green
    
    $response = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method POST -Body $json -Headers $headers -UseBasicParsing -Verbose
    
    Write-Host "SUCCESS!" -ForegroundColor Green
    Write-Host "Status Code: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "Response Headers:" -ForegroundColor Yellow
    $response.Headers | ForEach-Object { Write-Host "  $($_.Key): $($_.Value)" }
    Write-Host "Response Body:" -ForegroundColor Yellow
    Write-Host $response.Content -ForegroundColor White
    
} catch {
    Write-Host "ERROR OCCURRED:" -ForegroundColor Red
    Write-Host "Exception: $($_.Exception.Message)" -ForegroundColor Red
    
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        Write-Host "Status Description: $($_.Exception.Response.StatusDescription)" -ForegroundColor Red
        
        try {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "Error Response Body:" -ForegroundColor Yellow
            Write-Host $errorBody -ForegroundColor Red
        } catch {
            Write-Host "Could not read error response body" -ForegroundColor Red
        }
    }
}