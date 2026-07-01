# MiniChat 后端一键编译 + 启动
# 用法: .\start.ps1         -- 编译并启动全部
#       .\start.ps1 build   -- 仅编译
#       .\start.ps1 run     -- 仅启动（跳过编译）

param([string]$action = "")

Set-Location $PSScriptRoot

# 加载 .env 环境变量（OSS/SMS 密钥等）
$envFile = Join-Path $PSScriptRoot "..\deploy\.env"
if (Test-Path $envFile) {
  Get-Content $envFile | ForEach-Object {
    if ($_ -match '^\s*([A-Z_]+)=(.*)') {
      [Environment]::SetEnvironmentVariable($matches[1], $matches[2].Trim('"'), 'Process')
    }
  }
  Write-Host "=== 已加载 .env 环境变量 ===" -ForegroundColor Green
}

if ($action -ne "run") {
  Write-Host "=== 编译中... ===" -ForegroundColor Green
  mvn clean install -DskipTests
  Write-Host "=== 编译完成 ===" -ForegroundColor Green
}

if ($action -ne "build") {
  Write-Host "=== 启动 gateway (8080) ===" -ForegroundColor Cyan
  Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\minichat-gateway'; mvn spring-boot:run"

  Start-Sleep -Seconds 10

  Write-Host "=== 启动 minichat-user (8081) ===" -ForegroundColor Cyan
  Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\minichat-user'; mvn spring-boot:run"

  Start-Sleep -Seconds 10

  Write-Host "=== 启动 minichat-message (8082) ===" -ForegroundColor Cyan
  Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$PSScriptRoot\minichat-message'; mvn spring-boot:run"

  Write-Host "=== 全部已启动 ===" -ForegroundColor Green
}
