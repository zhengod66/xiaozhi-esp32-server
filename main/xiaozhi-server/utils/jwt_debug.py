#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
JWT令牌调试工具 - 用于分析和验证ESP32设备认证系统的JWT令牌

警告：本工具仅供开发和测试使用！
在生产环境中，JWT令牌应由manager-api生成和管理。
此工具不应用于生产环境的认证流程。
"""
import sys
import jwt
import json
import argparse
from datetime import datetime
import logging

# 配置简单日志
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def decode_jwt(token, secret=None, verify=False):
    """
    解码JWT令牌，可选是否验证签名
    
    Args:
        token: JWT令牌字符串
        secret: 用于验证签名的密钥(可选)
        verify: 是否验证签名
        
    Returns:
        bool: 解析是否成功
    """
    try:
        # 不验证签名的情况下解码头部和载荷
        header = jwt.get_unverified_header(token)
        print("\n=== JWT头部 ===")
        print(json.dumps(header, indent=2, ensure_ascii=False))
        
        # 解码载荷
        if verify and secret:
            # 验证签名
            payload = jwt.decode(token, secret, algorithms=[header.get('alg', 'HS256')])
            print("\n=== JWT载荷 (已验证签名) ===")
        else:
            # 不验证签名
            payload = jwt.decode(token, options={"verify_signature": False})
            print("\n=== JWT载荷 (未验证签名) ===")
            
        print(json.dumps(payload, indent=2, ensure_ascii=False))
        
        # 检查过期时间
        if 'exp' in payload:
            exp_time = datetime.fromtimestamp(payload['exp'])
            now = datetime.now()
            if exp_time > now:
                print(f"\n令牌有效期至: {exp_time} (还有 {(exp_time - now).total_seconds()/60:.1f} 分钟)")
            else:
                print(f"\n令牌已过期: {exp_time} (已过期 {(now - exp_time).total_seconds()/60:.1f} 分钟)")
        
        # 验证签名
        if verify and secret:
            try:
                jwt.decode(token, secret, algorithms=[header.get('alg', 'HS256')])
                print("\n签名验证: 有效")
            except jwt.InvalidSignatureError:
                print("\n签名验证: 无效 - 密钥不匹配")
        
        # 验证字段
        required_fields = ["deviceId", "macAddress"]
        missing_fields = [field for field in required_fields if field not in payload]
        if missing_fields:
            print(f"\n警告: 缺少必要字段: {', '.join(missing_fields)}")
        else:
            print("\n设备ID: " + payload.get("deviceId", "未指定"))
            print("MAC地址: " + payload.get("macAddress", "未指定"))
        
        return True
    except jwt.ExpiredSignatureError:
        print("\n错误: 令牌已过期")
    except jwt.InvalidTokenError as e:
        print(f"\n错误: 无效的令牌 - {str(e)}")
    except Exception as e:
        print(f"\n错误: {str(e)}")
    return False

def main():
    """主函数，处理命令行参数"""
    parser = argparse.ArgumentParser(description='JWT令牌调试工具')
    parser.add_argument('token', nargs='?', help='JWT令牌')
    parser.add_argument('--secret', help='用于验证签名的密钥')
    parser.add_argument('--verify', action='store_true', help='验证令牌签名')
    parser.add_argument('--config', help='从共享配置文件加载密钥')
    
    if len(sys.argv) == 1:
        parser.print_help()
        return
        
    args = parser.parse_args()
    
    if not args.token:
        print("请提供JWT令牌")
        return
    
    secret = args.secret
    
    # 尝试从配置文件加载密钥
    if args.config:
        try:
            with open(args.config, 'r', encoding='utf-8') as f:
                for line in f:
                    if '=' in line and not line.strip().startswith('#'):
                        key, value = line.strip().split('=', 1)
                        if key == "jwt.secret":
                            secret = value
                            print(f"已从配置文件加载密钥: {args.config}")
        except Exception as e:
            print(f"从配置文件加载密钥失败: {str(e)}")
    
    if args.verify and not secret:
        print("如果要验证签名，请提供密钥或配置文件")
        return
    
    print(f"分析JWT令牌: {args.token[:20]}...")
    decode_jwt(args.token, secret, args.verify)

if __name__ == "__main__":
    main() 