param([string]$BaseUrl = 'http://127.0.0.1:8085')
$ErrorActionPreference = 'Stop'
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$csrf = Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session
$headers = @{}; $headers[$csrf.headerName] = $csrf.token
$email = "smoke-$([Guid]::NewGuid().ToString('N'))@codequest.test"
$password = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(16))
$body = @{ email = $email; password = $password; displayName = 'Smoke Explorer' } | ConvertTo-Json
$registered = Invoke-RestMethod "$BaseUrl/api/auth/register" -Method Post -ContentType 'application/json' -Body $body -Headers $headers -WebSession $session
if ($registered.level -ne 1 -or $registered.totalXp -ne 0) { throw 'Initial player progress is incorrect.' }
$restored = Invoke-RestMethod "$BaseUrl/api/auth/me" -WebSession $session
if ($restored.id -ne $registered.id) { throw 'Session restore failed.' }
$cookie = $session.Cookies.GetCookies([Uri]$BaseUrl) | Where-Object Name -eq 'codequest_session'
if (-not $cookie.HttpOnly) { throw 'JWT cookie must be HttpOnly.' }
$csrf = Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session
$headers[$csrf.headerName] = $csrf.token
Invoke-RestMethod "$BaseUrl/api/auth/logout" -Method Post -Headers $headers -WebSession $session | Out-Null
$response = Invoke-WebRequest "$BaseUrl/api/auth/me" -WebSession $session -SkipHttpErrorCheck
if ($response.StatusCode -ne 401) { throw 'Logout did not clear the session.' }
$loginBody = @{ email = $email; password = $password } | ConvertTo-Json
$csrf = Invoke-RestMethod "$BaseUrl/api/auth/csrf" -WebSession $session
$headers[$csrf.headerName] = $csrf.token
$loggedIn = Invoke-RestMethod "$BaseUrl/api/auth/login" -Method Post -ContentType 'application/json' -Body $loginBody -Headers $headers -WebSession $session
if ($loggedIn.id -ne $registered.id) { throw 'Login failed.' }
$noCsrfSession = New-Object Microsoft.PowerShell.Commands.WebRequestSession
$noCsrfSession.Cookies = $session.Cookies
$rejected = Invoke-WebRequest "$BaseUrl/api/auth/logout" -Method Post -WebSession $noCsrfSession -SkipHttpErrorCheck
if ($rejected.StatusCode -ne 403) { throw 'Missing CSRF token was accepted.' }
Write-Output 'PASS: real HTTP registration, JWT cookie, session restore, logout, login, and CSRF rejection.'
Write-Output 'A synthetic Smoke Explorer account was created in the CodeQuest database.'
