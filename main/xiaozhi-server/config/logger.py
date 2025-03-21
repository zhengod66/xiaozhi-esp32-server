import os
import sys
from loguru import logger
from config.settings import load_config

def setup_logging():
    """从配置文件中读取日志配置，并设置日志输出格式和级别"""
    config = load_config()
    log_config = config["log"]
    
    # 修改日志格式，避免语法错误
    log_format = log_config.get(
        "log_format", 
        "<green>{time:YY-MM-DD HH:mm:ss}</green>[<light-blue>{extra[tag]}</light-blue>] - <level>{level}</level> - <light-green>{message}</light-green>"
    )
    
    # 使用更简单的条件逻辑，避免复杂的format_map解析错误
    safe_log_format = "<green>{time:YY-MM-DD HH:mm:ss}</green>[<light-blue>{extra[tag]}</light-blue>] - <level>{level}</level> - <light-green>{message}</light-green>"
    
    log_format_simple = log_config.get(
        "log_format_file", 
        "{time:YYYY-MM-DD HH:mm:ss} - {name} - {level} - {extra[tag]} - {message}"
    )
    
    log_level = log_config.get("log_level", "INFO")
    log_dir = log_config.get("log_dir", "tmp")
    log_file = log_config.get("log_file", "server.log")
    data_dir = log_config.get("data_dir", "data")

    os.makedirs(log_dir, exist_ok=True)
    os.makedirs(data_dir, exist_ok=True)

    # 配置日志输出
    # 先移除默认的处理器
    logger.remove()
    
    # 默认为所有日志添加一个tag字段，如果没有指定
    # 确保在配置中添加默认的tag值
    logger.configure(extra={"tag": "main"})

    # 输出到控制台，使用安全格式
    logger.add(sys.stdout, format=safe_log_format, level=log_level)

    # 输出到文件，使用安全格式
    logger.add(os.path.join(log_dir, log_file), format=log_format_simple, level=log_level)

    # 返回配置好的logger对象
    return logger
