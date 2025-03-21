import os
import logging
from pathlib import Path

# 配置日志
logger = logging.getLogger(__name__)

class JwtConfig:
    """JWT配置加载器，从共享配置文件加载JWT密钥"""
    
    def __init__(self):
        # 默认配置
        self.secret = "xiaozhi_esp32_jwt_secret_key_for_device_authentication_system"
        self.algorithm = "HS384"
        
        # 尝试从共享配置文件加载
        self._load_from_shared_config()
    
    def _load_from_shared_config(self):
        """从共享配置文件加载JWT配置"""
        try:
            # 查找共享配置文件路径
            base_path = Path(__file__).parent.parent.parent.parent
            config_path = base_path / "shared-config" / "jwt-secret.properties"
            
            if not config_path.exists():
                logger.warning(f"共享配置文件不存在: {config_path}，使用默认值")
                return
                
            # 读取配置
            with open(config_path, 'r', encoding='utf-8') as f:
                for line in f:
                    # 跳过注释和空行
                    if line.strip().startswith('#') or not line.strip():
                        continue
                        
                    if '=' in line:
                        key, value = line.strip().split('=', 1)
                        if key.strip() == "jwt.secret":
                            self.secret = value.strip()
                        elif key.strip() == "jwt.algorithm":
                            self.algorithm = value.strip()
            
            logger.info("已从共享配置文件加载JWT密钥配置")
        except Exception as e:
            logger.error(f"加载JWT配置失败: {str(e)}，使用默认值")

# 创建全局配置实例
jwt_config = JwtConfig() 