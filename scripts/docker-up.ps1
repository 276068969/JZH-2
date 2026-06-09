$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Import-DotEnv {
    param([string]$Path)

    Get-Content -Path $Path | ForEach-Object {
        $line = $_.Trim()
        if (-not $line -or $line.StartsWith("#")) {
            return
        }

        $parts = $line.Split("=", 2)
        if ($parts.Count -ne 2) {
            return
        }

        [Environment]::SetEnvironmentVariable($parts[0], $parts[1], "Process")
    }
}

function Assert-PortFree {
    param(
        [int]$Port,
        [string]$Name
    )

    $listeners = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
    if (-not $listeners) {
        return
    }

    foreach ($listener in $listeners) {
        $process = Get-Process -Id $listener.OwningProcess -ErrorAction SilentlyContinue
        $processName = if ($process) { $process.ProcessName } else { "unknown" }
        throw "$Name port $Port is already in use by PID $($listener.OwningProcess) ($processName) on $($listener.LocalAddress)."
    }
}

function Invoke-CurlHead {
    param([string]$Url)

    $response = & curl.exe -sS $Url
    if ($LASTEXITCODE -ne 0) {
        throw "Failed to request $Url."
    }

    return (($response -split "`n") | Select-Object -First 20) -join "`n"
}

function Wait-ForHttp {
    param(
        [string]$Url,
        [int]$TimeoutSeconds = 120
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $null = & curl.exe -sS $Url
            if ($LASTEXITCODE -eq 0) {
                return
            }
        } catch {
        }

        Start-Sleep -Seconds 2
    }

    throw "Timed out waiting for $Url."
}

$root = Split-Path -Parent $PSScriptRoot
Import-DotEnv -Path (Join-Path $root ".env")

$frontendPort = [int]$env:FRONTEND_PORT
$backendPort = [int]$env:BACKEND_PORT
$mysqlPort = [int]$env:MYSQL_PORT
$redisPort = [int]$env:REDIS_PORT

Push-Location $root
try {
    & docker compose down --remove-orphans
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose down --remove-orphans failed."
    }

    Assert-PortFree -Port $frontendPort -Name "frontend"
    Assert-PortFree -Port $backendPort -Name "backend"
    Assert-PortFree -Port $mysqlPort -Name "mysql"
    Assert-PortFree -Port $redisPort -Name "redis"

    & docker compose up --build -d
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose up --build -d failed."
    }

    $frontend127 = "http://127.0.0.1:$frontendPort"
    $frontendLocalhost = "http://localhost:$frontendPort"

    Wait-ForHttp -Url $frontend127
    Wait-ForHttp -Url $frontendLocalhost

    $head127 = Invoke-CurlHead -Url $frontend127
    $headLocalhost = Invoke-CurlHead -Url $frontendLocalhost
    if ($head127 -ne $headLocalhost) {
        throw "localhost and 127.0.0.1 returned different frontend HTML heads."
    }

    Write-Host ""
    Write-Host "Frontend URL: $frontendLocalhost"
    Write-Host "Frontend URL (127.0.0.1): $frontend127"
} finally {
    Pop-Location
}
