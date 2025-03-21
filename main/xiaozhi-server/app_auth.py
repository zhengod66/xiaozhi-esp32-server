import asyncio
import os
import logging
from config.settings import load_config, check_config_file
from core.websocket_server_auth import AuthWebSocketServer
from core.utils.util import check_ffmpeg_installed
from config.jwt_config import jwt_config

# 配置日志
logger = logging.getLogger(__name__)
TAG = __name__

async def main():
    """启动支持JWT认证的Xiaozhi Server"""
    # 检查配置和依赖
    check_config_file()
    check_ffmpeg_installed()
    
    # 加载配置
    config = load_config()
    
    # 添加JWT认证配置
    if "auth" not in config:
        config["auth"] = {
            "enabled": True,  # 默认启用认证
            "jwt": {
                "secret": jwt_config.secret,
                "algorithm": jwt_config.algorithm
            }
        }
    
    # 输出JWT配置信息
    logger.info(f"JWT认证配置已加载，算法: {jwt_config.algorithm}")
    
    # 创建并启动支持认证的WebSocket服务器
    ws_server = AuthWebSocketServer(config)
    ws_task = asyncio.create_task(ws_server.start())
    
    try:
        # 等待WebSocket服务器运行
        await ws_task
    except asyncio.CancelledError:
        logger.info("服务器任务被取消")
    except Exception as e:
        logger.error(f"服务器运行出错: {str(e)}")
    finally:
        ws_task.cancel()
        logger.info("服务器已关闭")

if __name__ == "__main__":
    # 配置日志格式
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # 运行应用
    asyncio.run(main()) 