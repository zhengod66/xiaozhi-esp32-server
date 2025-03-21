import asyncio
import websockets
import json
import logging
from typing import Dict, Any, Optional
from config.logger import setup_logging
from core.connection import ConnectionHandler
from core.connection_manager import connection_manager
from core.auth import AuthMiddleware, AuthenticationError
from core.utils.util import get_local_ip
from core.utils import asr, vad, llm, tts, memory, intent

TAG = __name__

class AuthWebSocketServer:
    """
    支持JWT认证的WebSocket服务器
    """
    
    def __init__(self, config: dict):
        """
        初始化服务器
        
        Args:
            config: 配置信息
        """
        self.config = config
        self.logger = setup_logging()
        
        # 添加认证相关配置
        self.auth_enabled = config.get("auth", {}).get("enabled", True)
        self.logger.info(f"认证功能状态: {'启用' if self.auth_enabled else '禁用'}")
        
        # 创建认证中间件实例
        self.auth_middleware = AuthMiddleware(config)
        
        # 初始化处理模块
        self._vad, self._asr, self._llm, self._tts, self._music, self._memory, self.intent = self._create_processing_instances()

    def _create_processing_instances(self):
        """
        创建处理模块实例
        
        Returns:
            tuple: 各个模块实例
        """
        from core.handle.musicHandler import MusicHandler
        
        # 检查是否禁用了各模块
        disable_vad = self.config.get("disable_vad", False)
        disable_asr = self.config.get("disable_asr", False)
        disable_llm = self.config.get("disable_llm", False) 
        disable_tts = self.config.get("disable_tts", False)
        disable_memory = self.config.get("disable_memory", False)
        disable_intent = self.config.get("disable_intent", False)
        
        # 创建空的占位模块
        class DummyModule:
            def __init__(self, name="dummy"):
                self.name = name
            def __getattr__(self, name):
                return lambda *args, **kwargs: None
        
        memory_cls_name = self.config["selected_module"].get("Memory", "nomem") # 默认使用nomem
        has_memory_cfg = self.config.get("Memory") and memory_cls_name in self.config["Memory"]
        memory_cfg = self.config["Memory"][memory_cls_name] if has_memory_cfg else {}

        # 根据配置创建实例或返回占位实例
        return (
            DummyModule("vad") if disable_vad else vad.create_instance(
                self.config["selected_module"]["VAD"],
                self.config["VAD"][self.config["selected_module"]["VAD"]]
            ),
            DummyModule("asr") if disable_asr else asr.create_instance(
                self.config["selected_module"]["ASR"]
                if not 'type' in self.config["ASR"][self.config["selected_module"]["ASR"]]
                else
                self.config["ASR"][self.config["selected_module"]["ASR"]]["type"],
                self.config["ASR"][self.config["selected_module"]["ASR"]],
                self.config["delete_audio"]
            ),
            DummyModule("llm") if disable_llm else llm.create_instance(
                self.config["selected_module"]["LLM"]
                if not 'type' in self.config["LLM"][self.config["selected_module"]["LLM"]]
                else
                self.config["LLM"][self.config["selected_module"]["LLM"]]['type'],
                self.config["LLM"][self.config["selected_module"]["LLM"]],
            ),
            DummyModule("tts") if disable_tts else tts.create_instance(
                self.config["selected_module"]["TTS"]
                if not 'type' in self.config["TTS"][self.config["selected_module"]["TTS"]]
                else
                self.config["TTS"][self.config["selected_module"]["TTS"]]["type"],
                self.config["TTS"][self.config["selected_module"]["TTS"]],
                self.config["delete_audio"]
            ),
            MusicHandler(self.config),
            DummyModule("memory") if disable_memory else memory.create_instance(memory_cls_name, memory_cfg),
            DummyModule("intent") if disable_intent else intent.create_instance(
                self.config["selected_module"]["Intent"]
                if not 'type' in self.config["Intent"][self.config["selected_module"]["Intent"]]
                else
                self.config["Intent"][self.config["selected_module"]["Intent"]]["type"],
                self.config["Intent"][self.config["selected_module"]["Intent"]]
            ),
        )

    async def start(self):
        """
        启动WebSocket服务器
        """
        server_config = self.config["server"]
        host = server_config["ip"]
        port = server_config["port"]

        self.logger.info(f"Server is running at ws://{get_local_ip()}:{port}")
        self.logger.info("=======上面的地址是websocket协议地址，请勿用浏览器访问=======")
        
        if self.auth_enabled:
            self.logger.info("JWT认证已启用，连接需要提供有效的JWT令牌")
        
        async with websockets.serve(
                self._handle_connection,
                host,
                port
        ):
            await asyncio.Future()

    async def _handle_connection(self, websocket):
        """
        处理新连接
        
        Args:
            websocket: WebSocket连接
        """
        device_info = None
        
        try:
            # 添加详细的请求调试信息
            # 安全获取路径，适配不同版本的websockets库
            try:
                ws_path = getattr(websocket, "path", None)
                if ws_path is None:
                    # 尝试从其他可能的属性获取路径
                    ws_path = getattr(websocket, "uri", getattr(websocket, "resource", "/"))
                    
                self.logger.bind(tag=TAG).info(f"收到新的WebSocket连接请求，路径: {ws_path}")
            except Exception as e:
                ws_path = "/"
                self.logger.bind(tag=TAG).warning(f"获取WebSocket路径失败: {str(e)}，使用默认路径")
                
            self.logger.bind(tag=TAG).info(f"WebSocket连接对象类型: {type(websocket).__name__}")
            
            try:
                self.logger.bind(tag=TAG).info(f"连接对象可用属性: {dir(websocket)[:20]}...")
            except Exception as e:
                self.logger.bind(tag=TAG).warning(f"获取WebSocket属性列表失败: {str(e)}")
            
            # 如果启用认证，则进行认证
            if self.auth_enabled:
                # 获取连接参数中的token
                token = None
                
                try:
                    # 尝试从查询参数获取token
                    # 安全处理路径
                    params = ""
                    if ws_path and "?" in ws_path:
                        params = ws_path.split("?", 1)[1]
                    self.logger.bind(tag=TAG).info(f"URL查询参数: {params}")
                    
                    pairs = [p.split("=", 1) for p in params.split("&") if "=" in p]
                    query_params = {key: value for key, value in pairs}
                    self.logger.bind(tag=TAG).info(f"解析后的查询参数: {query_params}")
                    
                    token = query_params.get("token")
                    if token:
                        self.logger.bind(tag=TAG).info(f"从URL查询参数获取到令牌 (前10个字符): {token[:10]}...")
                except Exception as e:
                    self.logger.bind(tag=TAG).error(f"解析URL查询参数时出错: {str(e)}")
                
                # 如果URL参数中没有token，尝试从Authorization头部获取
                if not token:
                    # 检查连接对象的headers属性或其他可能包含请求头的属性
                    self.logger.bind(tag=TAG).info("未从URL参数获取到令牌，尝试从请求头获取")
                    
                    # 安全获取头部信息
                    headers = None
                    # 尝试多种可能的头部属性
                    for attr_name in ["headers", "request_headers", "handshake_headers"]:
                        if hasattr(websocket, attr_name):
                            headers = getattr(websocket, attr_name)
                            self.logger.bind(tag=TAG).info(f"找到头部属性: {attr_name}")
                            break
                    
                    # 一种备用方法，尝试从request属性获取
                    if not headers and hasattr(websocket, "request"):
                        req = getattr(websocket, "request")
                        if hasattr(req, "headers"):
                            headers = req.headers
                            self.logger.bind(tag=TAG).info("从request.headers获取到头部信息")
                    
                    if headers:
                        self.logger.bind(tag=TAG).info(f"可用头部: {list(headers.keys()) if headers else '无'}")
                        
                        auth_header = headers.get("Authorization", "")
                        if auth_header:
                            self.logger.bind(tag=TAG).info(f"找到Authorization头: {auth_header[:15]}...")
                            if auth_header.startswith("Bearer "):
                                token = auth_header[7:]  # 移除 "Bearer " 前缀
                                self.logger.bind(tag=TAG).info(f"从Authorization头部获取到令牌 (前10个字符): {token[:10]}...")
                
                if not token:
                    self.logger.bind(tag=TAG).warning("WebSocket连接尝试缺少认证令牌")
                    await websocket.close(code=1008, reason="缺少认证令牌")
                    return
                
                try:
                    # 使用auth_middleware验证JWT令牌
                    self.logger.bind(tag=TAG).info("开始验证令牌...")
                    payload = self.auth_middleware.verify_token(token)
                    # 提取设备信息
                    device_id = payload.get("deviceId", "unknown")
                    mac_address = payload.get("macAddress", "unknown")
                    self.logger.bind(tag=TAG).info(f"设备认证成功: ID={device_id}, MAC={mac_address}")
                    
                    # 记录设备信息
                    device_info = {
                        "device_id": device_id,
                        "mac_address": mac_address
                    }
                except AuthenticationError as e:
                    self.logger.bind(tag=TAG).warning(f"JWT令牌验证失败: {e.message}")
                    await websocket.close(code=1008, reason=e.message)
                    return
                except Exception as e:
                    self.logger.bind(tag=TAG).error(f"认证处理错误: {str(e)}")
                    await websocket.close(code=1011, reason="认证处理错误")
                    return
                
                # 更新连接管理器
                await connection_manager.connect(websocket, device_id)
            
            # 创建处理器并处理连接
            handler = ConnectionHandler(
                self.config, 
                self._vad, 
                self._asr, 
                self._llm, 
                self._tts, 
                self._music, 
                self._memory, 
                self.intent
            )
            
            # 如果有设备信息，添加到处理器中
            if device_info:
                # 这里可以根据需要修改ConnectionHandler以支持设备信息
                # 暂时只是记录设备信息
                self.logger.info(f"处理设备 {device_info['device_id']} 的连接")
            
            try:
                # 处理连接
                await handler.handle_connection(websocket)
            finally:
                # 连接结束时更新状态
                if device_info:
                    device_id = device_info["device_id"]
                    await connection_manager.disconnect(websocket, device_id)
        
        except Exception as e:
            self.logger.error(f"处理WebSocket连接时出错: {str(e)}")
            # 确保连接断开
            try:
                await websocket.close(code=1011, reason="服务器内部错误")
            except:
                pass
            
            # 如果有设备信息，更新连接状态
            if device_info:
                device_id = device_info["device_id"]
                await connection_manager.disconnect(websocket, device_id) 