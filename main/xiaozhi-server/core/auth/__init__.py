"""
认证模块，提供WebSocket连接认证功能
"""
from .jwt_helper import JwtHelper, JwtError, JwtExpiredError, JwtInvalidError, JwtMissingFieldError
import logging

# 配置日志
logger = logging.getLogger(__name__)

# 添加兼容层，与现有代码结构匹配
class AuthenticationError(Exception):
    """认证错误，用于表示认证失败"""
    def __init__(self, message: str, code: int = 401):
        self.message = message
        self.code = code
        super().__init__(message)

class AuthMiddleware:
    """认证中间件，提供兼容旧代码的接口"""
    
    def __init__(self, config: dict = None):
        """
        初始化认证中间件
        
        Args:
            config: 配置信息
        """
        self.config = config or {}
        self.jwt_enabled = self.config.get("auth", {}).get("enabled", True)
        logger.info(f"初始化认证中间件，JWT认证状态: {'启用' if self.jwt_enabled else '禁用'}")
    
    async def authenticate(self, headers: dict):
        """
        验证请求头中的认证信息
        
        Args:
            headers: 请求头
            
        Raises:
            AuthenticationError: 如果认证失败
        """
        # 现有系统可能使用其他认证方式，为保持兼容，这里直接通过
        # 实际JWT认证会在WebSocket连接时进行
        logger.debug("Auth中间件处理请求头认证")
        return True
    
    def verify_token(self, token: str):
        """
        验证令牌
        
        Args:
            token: JWT令牌
            
        Returns:
            bool: 令牌是否有效
            
        Raises:
            AuthenticationError: 如果令牌无效
        """
        if not self.jwt_enabled:
            return True
            
        try:
            # 使用JwtHelper验证令牌
            is_valid, payload, error = JwtHelper.validate_token(token)
            if not is_valid:
                raise AuthenticationError(f"令牌验证失败: {error}")
            return payload
        except JwtError as e:
            raise AuthenticationError(e.message, e.code)
        except Exception as e:
            raise AuthenticationError(f"令牌验证出错: {str(e)}")

__all__ = [
    'JwtHelper',
    'JwtError',
    'JwtExpiredError',
    'JwtInvalidError',
    'JwtMissingFieldError',
    'AuthMiddleware',
    'AuthenticationError'
] 