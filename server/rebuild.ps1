# MiniChat 重新编译全部模块
Write-Host "=== 重新编译全部模块 ===" -ForegroundColor Green
mvn clean install -DskipTests
Write-Host "=== 完成，可以启动服务 ===" -ForegroundColor Green
Write-Host "  .\start.ps1 run   -- 直接启动（跳过编译）"
Write-Host "  .\start.ps1       -- 编译 + 启动"
