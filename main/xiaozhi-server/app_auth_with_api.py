import asyncio
import os
import logging
import threading
import yaml
from config.settings import load_config
from core.websocket_server_auth import AuthWebSocketServer
from config.jwt_config import jwt_config
from api_server import start_api_server

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

def start_api_server_thread(host, port):
    """
    在单独的线程中启动API服务器
    
    Args:
        host: API服务器主机
        port: API服务器端口
    """
    logger.info(f"启动API服务器: http://{host}:{port}")
    start_api_server(host, port)

async def main():
    """启动支持JWT认证的Xiaozhi Server和API服务器"""
    # 加载测试配置文件
    test_config_path = os.path.join(os.path.dirname(__file__), "test_config.yaml")
    if os.path.exists(test_config_path):
        logger.info(f"使用测试配置文件: {test_config_path}")
        with open(test_config_path, 'r', encoding='utf-8') as f:
            config = yaml.safe_load(f)
    else:
        # 加载默认配置
        logger.info("测试配置文件不存在，使用默认配置")
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
    
    # 添加API服务器配置
    if "api_server" not in config:
        config["api_server"] = {
            "enabled": True,  # 默认启用API服务器
            "host": "0.0.0.0",
            "port": 8080
        }
    
    # 输出JWT配置信息
    logger.info(f"JWT认证配置已加载，算法: {jwt_config.algorithm}")
    
    # 如果启用了API服务器，则启动
    if config["api_server"].get("enabled", True):
        api_host = config["api_server"].get("host", "0.0.0.0")
        api_port = config["api_server"].get("port", 8080)
        api_thread = threading.Thread(
            target=start_api_server_thread,
            args=(api_host, api_port),
            daemon=True
        )
        api_thread.start()
        logger.info(f"API服务器线程已启动，监听于 {api_host}:{api_port}")
    
    # 创建并启动支持认证的WebSocket服务器
    ws_server = AuthWebSocketServer(config)
    ws_task = asyncio.create_task(ws_server.start())
    
    try:
        # 等待WebSocket服务器运行
        await ws_task
    except asyncio.CancelledError:
        logger.info("WebSocket服务器任务被取消")
    except Exception as e:
        logger.error(f"WebSocket服务器运行出错: {str(e)}")
    finally:
        ws_task.cancel()
        logger.info("WebSocket服务器已关闭")

if __name__ == "__main__":
    try:
        # 运行应用
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("收到键盘中断，服务器关闭")
    except Exception as e:
        logger.error(f"主程序运行出错: {str(e)}") 