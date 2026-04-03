# JWT Token Test Script - Copy & Paste Commands

# Test your JWT implementation step by step

# ===== STEP 1: REGISTER USER =====
# Copy and run in PowerShell:

$registerData = @{
    username="testuser123"
    password="testpass123456"
    email="testuser@example.com"
    fullName="Test User"
} | ConvertTo-Json

curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d $registerData

Write-Host "Expected: 201 Created with user data"
Write-Host ""

# ===== STEP 2: LOGIN TO GET TOKEN =====
# Copy and run in PowerShell:

$loginData = @{
    email="testuser@example.com"
    password="testpass123456"
} | ConvertTo-Json

$loginResponse = curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d $loginData | ConvertFrom-Json

$token = $loginResponse.accessToken

Write-Host "Token received: $token"
Write-Host ""

# ===== STEP 3: TEST CART ADD WITH TOKEN =====
# This should work now!

$cartData = @{
    productId=1
    quantity=2
} | ConvertTo-Json

curl -X POST http://localhost:8080/api/cart/add `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d $cartData `
  -v

Write-Host ""
Write-Host "Expected Response:"
Write-Host '{"success":true,"message":"Product added to cart successfully",...}'
Write-Host ""
Write-Host "Check console logs for:"
Write-Host '✅ [JwtAuthFilter] JWT validated!'
Write-Host '✅ [CartController] Item added to cart successfully'

