#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
JWT令牌生成工具 - 用于生成ESP32设备认证系统的测试JWT令牌

警告：本工具仅供开发和测试使用！
在生产环境中，JWT令牌应由manager-api生成和管理。
此工具不应用于生产环境的认证流程。
"""
import jwt
import json
import time
import argparse
import sys
from datetime import datetime, timedelta
from pathlib import Path
import logging

# 配置简单日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def load_secret_from_config(config_path=None):
    """从配置文件加载JWT密钥"""
    secret = "xiaozhi_esp32_jwt_secret_key_for_device_authentication_system"
    algorithm = "HS384"
    
    if not config_path:
        # 尝试查找默认配置路径
        try:
            base_path = Path(__file__).parent.parent.parent.parent
            config_path = base_path / "shared-config" / "jwt-secret.properties"
        except:
            logger.warning("无法确定默认配置路径，使用硬编码密钥")
            return secret, algorithm
    
    # 从配置文件加载
    try:
        if not Path(config_path).exists():
            logger.warning(f"配置文件不存在: {config_path}，使用默认密钥")
            return secret, algorithm
            
        with open(config_path, 'r', encoding='utf-8') as f:
            for line in f:
                if '=' in line and not line.strip().startswith('#'):
                    key, value = line.strip().split('=', 1)
                    if key == "jwt.secret":
                        secret = value
                    elif key == "jwt.algorithm":
                        algorithm = value
        
        logger.info(f"已从配置文件加载JWT密钥: {config_path}")
    except Exception as e:
        logger.error(f"加载配置文件失败: {str(e)}，使用默认密钥")
    
    return secret, algorithm

def generate_token(device_id, mac_address, expiry_hours=24, secret=None, algorithm=None, config_path=None):
    """
    生成JWT令牌
    
    Args:
        device_id: 设备ID
        mac_address: MAC地址
        expiry_hours: 过期时间(小时)
        secret: 密钥(可选)
        algorithm: 算法(可选)
        config_path: 配置文件路径(可选)
        
    Returns:
        str: 生成的JWT令牌
    """
    # 如果未提供密钥，从配置加载
    if not secret or not algorithm:
        loaded_secret, loaded_alg = load_secret_from_config(config_path)
        secret = secret or loaded_secret
        algorithm = algorithm or loaded_alg
    
    # 创建载荷
    payload = {
        "deviceId": device_id,
        "macAddress": mac_address,
        "exp": int((datetime.now() + timedelta(hours=expiry_hours)).timestamp()),
        "iat": int(time.time())
    }
    
    # 创建令牌
    token = jwt.encode(payload, secret, algorithm=algorithm)
    
    return token

def main():
    """主函数，处理命令行参数"""
    parser = argparse.ArgumentParser(description='JWT令牌生成工具')
    parser.add_argument('--device-id', default="test-device-001", help='设备ID')
    parser.add_argument('--mac', default="AA:BB:CC:DD:EE:FF", help='MAC地址')
    parser.add_argument('--hours', type=int, default=24, help='令牌有效期(小时)')
    parser.add_argument('--secret', help='自定义密钥')
    parser.add_argument('--algorithm', help='自定义算法')
    parser.add_argument('--config', help='配置文件路径')
    parser.add_argument('--print-url', action='store_true', help='生成WebSocket URL格式(带token参数)')
    parser.add_argument('--server', default="localhost:8000", help='WebSocket服务器地址')
    
    args = parser.parse_args()
    
    # 生成令牌
    token = generate_token(
        args.device_id,
        args.mac,
        args.hours,
        args.secret,
        args.algorithm,
        args.config
    )
    
    # 打印结果
    print("\n=== 生成的JWT令牌 ===")
    print(token)
    print(f"\n=== 令牌有效期: {args.hours}小时 ===")
    
    # 如果需要，生成带令牌的URL
    if args.print_url:
        url = f"ws://{args.server}/ws?token={token}"
        print("\n=== WebSocket连接URL (用于ESP32) ===")
        print(url)
    
    print("\n=== 令牌信息 ===")
    print(f"设备ID: {args.device_id}")
    print(f"MAC地址: {args.mac}")
    print(f"签名算法: {args.algorithm or '从配置加载'}")

if __name__ == "__main__":
    main() 