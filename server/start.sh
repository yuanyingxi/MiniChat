#!/bin/bash
# MiniChat 后端一键编译 + 启动
# 用法: bash start.sh        -- 编译并启动全部
#       bash start.sh build  -- 仅编译
#       bash start.sh run    -- 仅启动（跳过编译）

set -e
cd "$(dirname "$0")"

if [ "$1" != "run" ]; then
  echo "=== 编译中... ==="
  mvn clean install -DskipTests
  echo "=== 编译完成 ==="
fi

if [ "$1" != "build" ]; then
  echo "=== 启动 gateway (8080) ==="
  (cd minichat-gateway && mvn spring-boot:run) &
  sleep 8
  echo "=== 启动 minichat-user (8081) ==="
  (cd minichat-user && mvn spring-boot:run) &
  sleep 8
  echo "=== 启动 minichat-message (8082) ==="
  (cd minichat-message && mvn spring-boot:run) &
  echo "=== 全部已启动 ==="
  wait
fi
