param([string]$Path = 'content/dsa/arrays/v1.json', [switch]$ValidateOnly)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$resolved = (Resolve-Path -LiteralPath $Path).Path
foreach ($line in Get-Content -LiteralPath (Join-Path $root '.env')) {
    if ($line -match '^([A-Z_]+)=(.*)$') { [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2], 'Process') }
}
$maven = Join-Path $root '.tools/apache-maven-3.9.9/bin/mvn.cmd'
$importArgs = '--server.port=0 --pack-import'
if ($ValidateOnly) { $importArgs += ' --validate-only' }
$previousFile=$env:CODEQUEST_PACK_FILE
try {
    $env:CODEQUEST_PACK_FILE=$resolved
    & $maven "-Dmaven.repo.local=$root/.tools/m2" -f "$root/backend/pom.xml" spring-boot:run "-Dspring-boot.run.arguments=$importArgs"
    $code=$LASTEXITCODE
} finally { $env:CODEQUEST_PACK_FILE=$previousFile }
exit $code
