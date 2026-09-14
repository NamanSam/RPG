param([ValidateSet('run','test','package')][string]$Action = 'run')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$mavenCommand = Join-Path $projectRoot '.tools\apache-maven-3.9.9\bin\mvn.cmd'
if (-not (Test-Path -LiteralPath $mavenCommand)) {
    $installedMaven = Get-Command mvn.cmd -ErrorAction SilentlyContinue
    if ($installedMaven) { $mavenCommand = $installedMaven.Source }
    else { throw 'Maven is missing. Run .\scripts\setup-maven.ps1 first.' }
}
$envFile = Join-Path $projectRoot '.env'
if (Test-Path -LiteralPath $envFile) {
    foreach ($line in Get-Content -LiteralPath $envFile) {
        if ($line -match '^([A-Z_]+)=(.*)$') { [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process') }
    }
}
$goal = switch ($Action) { 'run' { 'spring-boot:run' } 'test' { 'test' } 'package' { 'package' } }
& $mavenCommand "-Dmaven.repo.local=$projectRoot\.tools\m2" -f "$projectRoot\backend\pom.xml" $goal
exit $LASTEXITCODE
