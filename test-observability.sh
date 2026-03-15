#!/bin/bash

echo "=== 可观测性测试 ==="
echo ""

# 1. 测试聊天接口
echo "1. 发送测试请求..."
curl -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1001,
    "sessionId": 2001,
    "prompt": "你好"
  }' \
  -v 2>&1 | grep -E "(X-Request-ID|HTTP)"

echo ""
echo "2. 查看 Prometheus 指标..."
curl -s http://localhost:8080/actuator/prometheus | grep -E "(ai_model|rag_retrieval)" | head -20

echo ""
echo "=== 测试完成 ==="
echo ""
echo "请检查应用日志，确认包含以下信息："
echo "  - request_id"
echo "  - session_id=2001"
echo "  - user_id=1001"
echo "  - [LLM_REQUEST]"
echo "  - [LLM_RESPONSE] SUCCESS"
