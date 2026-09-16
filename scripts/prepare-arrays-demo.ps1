# Explicit localhost-only test fixture; no fixture code ships in the API jar.
param([string]$Email = 'arrays-demo@codequest.test', [string]$Password = 'Arrays-demo-only-2026!')
$ErrorActionPreference = 'Stop'
if ($Email -notmatch '^arrays-(demo|smoke-[a-f0-9]+)@codequest\.test$') { throw 'Only dedicated synthetic Array Isles accounts are allowed.' }
$root = Split-Path -Parent $PSScriptRoot
foreach ($line in Get-Content -LiteralPath (Join-Path $root '.env')) {
    if ($line -match '^([A-Z_]+)=(.*)$') { [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process') }
}
if ($env:DB_URL -notmatch '^jdbc:mysql://localhost:3307/codequest(?:\?|$)') { throw 'Fixture requires the dedicated local CodeQuest database on port 3307.' }
$keys=@('SPRING_DATASOURCE_URL','SPRING_DATASOURCE_USERNAME','SPRING_DATASOURCE_PASSWORD','CODEQUEST_ARRAYS_FIXTURE','CODEQUEST_ARRAYS_EMAIL','CODEQUEST_ARRAYS_PASSWORD')
$saved=@{}; foreach($key in $keys) { $saved[$key]=[Environment]::GetEnvironmentVariable($key,'Process') }
try {
    $env:SPRING_DATASOURCE_URL=$env:DB_URL; $env:SPRING_DATASOURCE_USERNAME=$env:DB_USERNAME; $env:SPRING_DATASOURCE_PASSWORD=$env:DB_PASSWORD
    $env:CODEQUEST_ARRAYS_FIXTURE='true'; $env:CODEQUEST_ARRAYS_EMAIL=$Email; $env:CODEQUEST_ARRAYS_PASSWORD=$Password
    & (Join-Path $root '.tools/apache-maven-3.9.9/bin/mvn.cmd') "-Dmaven.repo.local=$root/.tools/m2" -f "$root/backend/pom.xml" '-Dtest=ArrayDemoFixtureTest' test
    if($LASTEXITCODE -ne 0) { throw 'Demo fixture failed.' }
    Write-Output "Local demonstration ready: $Email at http://127.0.0.1:5173/dsa"
} finally { foreach($key in $keys) { [Environment]::SetEnvironmentVariable($key,$saved[$key],'Process') } }
