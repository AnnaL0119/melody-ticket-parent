#!/bin/bash
# MelodyTicket 一键启动脚本
# 使用方法: bash start.sh [build|up|down|restart|logs]

set -e

COMPOSE_FILE="docker-compose.yml"
ENV_FILE=".env"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 检查必要工具
check_dependencies() {
    if ! command -v docker &> /dev/null; then
        echo -e "${RED}错误: 未安装 Docker${NC}"
        exit 1
    fi
    if ! command -v docker-compose &> /dev/null; then
        echo -e "${RED}错误: 未安装 docker-compose${NC}"
        exit 1
    fi
}

# 显示帮助
show_help() {
    echo "MelodyTicket Docker 部署管理脚本"
    echo ""
    echo "用法: bash start.sh [选项]"
    echo ""
    echo "选项:"
    echo "  build    构建所有镜像并启动"
    echo "  up       启动所有服务"
    echo "  down     停止并移除所有容器"
    echo "  restart  重启所有服务"
    echo "  logs     查看日志"
    echo "  status   查看服务状态"
    echo "  clean    清理所有数据（包括数据卷）"
    echo ""
    echo "示例:"
    echo "  bash start.sh build    # 首次部署"
    echo "  bash start.sh up       # 启动服务"
    echo "  bash start.sh logs     # 查看日志"
}

# 构建镜像
build_images() {
    echo -e "${YELLOW}[1/3] Maven 打包...${NC}"
    cd ..
    mvn clean package -DskipTests -pl common,user-service,concert-service,ticket-service,web-client -am
    echo -e "${GREEN}Maven 打包完成${NC}"

    cd docker

    echo -e "${YELLOW}[2/3] 构建 Docker 镜像...${NC}"
    docker-compose -f ${COMPOSE_FILE} build
    echo -e "${GREEN}Docker 镜像构建完成${NC}"

    echo -e "${YELLOW}[3/3] 启动服务...${NC}"
    docker-compose -f ${COMPOSE_FILE} --env-file ${ENV_FILE} up -d
    echo -e "${GREEN}所有服务已启动${NC}"
}

# 启动服务
start_services() {
    echo -e "${YELLOW}启动所有服务...${NC}"
    docker-compose -f ${COMPOSE_FILE} --env-file ${ENV_FILE} up -d
    echo -e "${GREEN}所有服务已启动${NC}"
    show_status
}

# 停止服务
stop_services() {
    echo -e "${YELLOW}停止所有服务...${NC}"
    docker-compose -f ${COMPOSE_FILE} down
    echo -e "${GREEN}所有服务已停止${NC}"
}

# 重启服务
restart_services() {
    echo -e "${YELLOW}重启所有服务...${NC}"
    docker-compose -f ${COMPOSE_FILE} restart
    echo -e "${GREEN}所有服务已重启${NC}"
}

# 查看日志
show_logs() {
    docker-compose -f ${COMPOSE_FILE} logs -f
}

# 查看状态
show_status() {
    echo -e "${GREEN}服务状态:${NC}"
    docker-compose -f ${COMPOSE_FILE} ps

    echo ""
    echo -e "${GREEN}访问地址:${NC}"
    echo "  Gateway API:     http://localhost:8080"
    echo "  Nacos 控制台:    http://localhost:8848/nacos"
    echo "  Sentinel 控制台: http://localhost:8010"
    echo "  SkyWalking UI:   http://localhost:8088"
    echo "  前端页面:        http://localhost:8080 (通过Gateway)"
}

# 清理数据
clean_data() {
    echo -e "${RED}警告: 这将删除所有容器和数据卷！${NC}"
    read -p "确认继续? (y/N) " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        docker-compose -f ${COMPOSE_FILE} down -v
        echo -e "${GREEN}所有数据已清理${NC}"
    else
        echo "取消清理"
    fi
}

# 主函数
main() {
    check_dependencies

    case "${1:-help}" in
        build)
            build_images
            ;;
        up)
            start_services
            ;;
        down)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        logs)
            show_logs
            ;;
        status)
            show_status
            ;;
        clean)
            clean_data
            ;;
        help|*)
            show_help
            ;;
    esac
}

main "$@"
