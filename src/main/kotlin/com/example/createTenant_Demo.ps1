$aa="DTRETEST"
$body = @{
    companyName = "TestCompany$aa"
    companyEmail = "test@company$aa.com"
    adminUsername = "admin_user$aa"
    adminEmail = "admin@company$aa.com"
    adminPassword = "SecurePassword123!"
    populateDemo= $true
} | ConvertTo-Json

curl.exe -X POST http://localhost:8080/onboarding/register `
  -H "Content-Type: application/json" `
  -d $body