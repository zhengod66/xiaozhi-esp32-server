import jwt
import logging
from datetime import datetime
from typing import Dict, Optional, Any, Tuple
from config.jwt_config import jwt_config

# 配置日志
logger = logging.getLogger(__name__)

class JwtError(Exception):
    """JWT验证错误基类"""
    def __init__(self, message: str, code: int = 1008):
        self.message = message
        self.code = code
        super().__init__(message)

class JwtExpiredError(JwtError):
    """JWT令牌过期错误"""
    def __init__(self):
        super().__init__("令牌已过期", 1008)

class JwtInvalidError(JwtError):
    """JWT令牌无效错误"""
    def __init__(self, details: str = "无效的令牌"):
        super().__init__(details, 1008)

class JwtMissingFieldError(JwtError):
    """JWT令牌缺少必要字段错误"""
    def __init__(self, field: str):
        super().__init__(f"令牌缺少必要字段: {field}", 1008)

class JwtHelper:
    """JWT验证工具类"""
    
    @staticmethod
    def decode_token(token: str) -> Dict[str, Any]:
        """
        解析并验证JWT令牌
        
        Args:
            token: JWT令牌字符串
            
        Returns:
            Dict: 解析后的JWT载荷
            
        Raises:
            JwtExpiredError: 令牌已过期
            JwtInvalidError: 令牌无效
            JwtMissingFieldError: 令牌缺少必要字段
        """
        try:
            # 使用共享密钥解码JWT
            payload = jwt.decode(
                token, 
                jwt_config.secret, 
                algorithms=[jwt_config.algorithm]
            )
            
            # 验证必要字段
            if "deviceId" not in payload:
                raise JwtMissingFieldError("deviceId")
                
            if "macAddress" not in payload:
                raise JwtMissingFieldError("macAddress")
            
            # 检查令牌是否过期
            if JwtHelper.is_token_expired(payload):
                raise JwtExpiredError()
                
            return payload
            
        except jwt.ExpiredSignatureError:
            logger.warning("令牌已过期")
            raise JwtExpiredError()
        except jwt.InvalidTokenError as e:
            logger.warning(f"无效的令牌: {str(e)}")
            raise JwtInvalidError(str(e))
        except Exception as e:
            logger.error(f"令牌验证错误: {str(e)}")
            raise JwtInvalidError(f"令牌验证失败: {str(e)}")
    
    @staticmethod
    def validate_token(token: str) -> Tuple[bool, Optional[Dict[str, Any]], Optional[str]]:
        """
        验证令牌并返回结果
        
        Args:
            token: JWT令牌字符串
            
        Returns:
            Tuple[bool, Optional[Dict], Optional[str]]: 
                - 验证是否成功
                - 成功时返回载荷，失败时为None
                - 失败时返回错误消息，成功时为None
        """
        try:
            payload = JwtHelper.decode_token(token)
            return True, payload, None
        except JwtError as e:
            return False, None, e.message
        except Exception as e:
            return False, None, str(e)
    
    @staticmethod
    def is_token_expired(payload: Dict[str, Any]) -> bool:
        """
        检查令牌是否过期
        
        Args:
            payload: JWT载荷
            
        Returns:
            bool: 是否已过期
        """
        exp = payload.get("exp", 0)
        current_time = datetime.utcnow().timestamp()
        return current_time > exp
    
    @staticmethod
    def get_device_info(payload: Dict[str, Any]) -> Tuple[str, str]:
        """
        从JWT载荷中提取设备信息
        
        Args:
            payload: JWT载荷
            
        Returns:
            Tuple[str, str]: 设备ID和MAC地址
        """
        device_id = str(payload.get("deviceId", ""))
        mac_address = payload.get("macAddress", "")
        return device_id, mac_address 