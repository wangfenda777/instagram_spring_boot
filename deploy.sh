#!/bin/bash

# ============ 配置区 ============
SERVER_IP="112.124.47.169"
SERVER_USER="root"
REMOTE_DIR="/root"
JAR_NAME="instagram-1.0.0.jar"
APP_LOG="${REMOTE_DIR}/instagram.log"
# ================================

echo "===== 1. 本地打包 ====="
mvn clean package -DskipTests -q
if [ $? -ne 0 ]; then
    echo "打包失败，请检查代码！"
    exit 1
fi
echo "打包成功: target/${JAR_NAME}"

echo ""
echo "===== 2. 上传到服务器 ====="
scp "target/${JAR_NAME}" "${SERVER_USER}@${SERVER_IP}:${REMOTE_DIR}/"
if [ $? -ne 0 ]; then
    echo "上传失败，请检查服务器连接！"
    exit 1
fi
echo "上传成功"

echo ""
echo "===== 3. 重启服务 ====="
ssh "${SERVER_USER}@${SERVER_IP}" << EOF
    echo "停止旧进程..."
    PID=\$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print \$2}')
    if [ -n "\$PID" ]; then
        kill -9 \$PID
        echo "已停止旧进程: \$PID"
    else
        echo "没有旧进程在运行"
    fi

    echo "启动新服务..."
    nohup java -jar ${REMOTE_DIR}/${JAR_NAME} > ${APP_LOG} 2>&1 &
    sleep 3

    NEW_PID=\$(ps -ef | grep "${JAR_NAME}" | grep -v grep | awk '{print \$2}')
    if [ -n "\$NEW_PID" ]; then
        echo "启动成功！PID: \$NEW_PID"
        echo "日志文件: ${APP_LOG}"
    else
        echo "启动失败，请查看日志: ${APP_LOG}"
    fi
EOF

echo ""
echo "===== 部署完成 ====="
echo "接口文档: http://${SERVER_IP}:8081/doc.html"
