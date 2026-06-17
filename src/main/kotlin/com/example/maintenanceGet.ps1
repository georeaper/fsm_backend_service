$loginResponse = curl.exe -s -X POST http://localhost:8080/auth/login `
  -H "Content-Type: application/json" `
  -d '{"username":"george_mk","password":"Giorgos13"}'

$token = ($loginResponse | ConvertFrom-Json).token

curl.exe http://localhost:8080/maintenances `
  -H "Authorization: Bearer $token"