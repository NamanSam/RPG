# Starts only the already-initialized CodeQuest data directory. Never uses the installed service's configuration.
param([string]$MySqlExecutable = 'C:\Program Files\MySQL\MySQL Server 26.7\bin\mysqld.exe')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$dataDirectory = Join-Path $projectRoot '.tools\mysql-data'
if (-not (Test-Path -LiteralPath (Join-Path $dataDirectory 'auto.cnf'))) {
    throw 'CodeQuest local data is not initialized. Use the Docker setup in README.md for a fresh installation.'
}
if (-not (Test-Path -LiteralPath $MySqlExecutable)) { throw 'Set -MySqlExecutable to your installed mysqld.exe path.' }
& $MySqlExecutable --no-defaults "--datadir=$dataDirectory" --bind-address=127.0.0.1 --port=3307 --mysqlx=OFF --console
exit $LASTEXITCODE
