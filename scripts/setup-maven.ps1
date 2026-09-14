$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$toolsDirectory = Join-Path $projectRoot '.tools'
$archivePath = Join-Path $toolsDirectory 'maven.zip'
$mavenUrl = 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip'
New-Item -ItemType Directory -Force $toolsDirectory | Out-Null
Invoke-WebRequest -Uri $mavenUrl -OutFile $archivePath
$expectedHash = (Invoke-WebRequest -Uri "$mavenUrl.sha512").Content.Trim().Split(' ')[0]
if ((Get-FileHash -LiteralPath $archivePath -Algorithm SHA512).Hash -ne $expectedHash) { throw 'Maven checksum verification failed.' }
Expand-Archive -LiteralPath $archivePath -DestinationPath $toolsDirectory -Force
Write-Output 'Project-local Maven is ready. No system settings were changed.'
