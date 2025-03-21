import json
from config.logger import setup_logging

TAG = __name__
logger = setup_logging()


async def handleHelloMessage(conn):
    """
    处理客户端发送的hello消息，回复服务器的hello消息
    
    客户端期望的响应格式：
    {
        "type": "hello",
        "transport": "websocket",
        "audio_params": {
            "sample_rate": 16000
        }
    }
    """
    # 确保welcome_msg包含所有必要字段
    if conn.welcome_msg is None or "type" not in conn.welcome_msg:
        logger.bind(tag=TAG).warning("欢迎消息格式不正确，创建新的hello消息")
        conn.welcome_msg = {
            "type": "hello",
            "transport": "websocket",
            "audio_params": {
                "sample_rate": 16000,
                "format": "opus",
                "channels": 1
            },
            "session_id": conn.session_id
        }
    elif conn.welcome_msg.get("type") != "hello":
        logger.bind(tag=TAG).warning(f"欢迎消息类型错误: {conn.welcome_msg.get('type')}，将修改为'hello'")
        conn.welcome_msg["type"] = "hello"
    
    # 确保包含必要的音频参数
    if "audio_params" not in conn.welcome_msg:
        conn.welcome_msg["audio_params"] = {
            "sample_rate": 16000,
            "format": "opus",
            "channels": 1
        }
    
    # 确保包含transport字段
    if "transport" not in conn.welcome_msg:
        conn.welcome_msg["transport"] = "websocket"
    
    logger.bind(tag=TAG).info(f"发送hello响应: {conn.welcome_msg}")
    await conn.websocket.send(json.dumps(conn.welcome_msg))
