import logging
import websockets
from typing import Dict, Set, Optional
import time
import asyncio
from datetime import datetime

logger = logging.getLogger(__name__)

class ConnectionManager:
    """
    WebSocket连接管理器，负责跟踪和管理设备连接
    """
    
    def __init__(self):
        # 活跃连接映射 {device_id: {websocket, ...}}
        self.active_connections: Dict[str, Set[websockets.WebSocketServerProtocol]] = {}
        
        # 设备状态映射 {device_id: status}
        # 状态: "online", "offline", "error"
        self.device_status: Dict[str, str] = {}
        
        # 连接时间映射 {device_id: connect_time}
        self.connection_times: Dict[str, float] = {}
        
        # 最后活动时间 {device_id: last_activity_time}
        self.last_activity: Dict[str, float] = {}
        
        logger.info("连接管理器已初始化")
    
    async def connect(self, websocket: websockets.WebSocketServerProtocol, device_id: str) -> bool:
        """
        建立新连接
        
        Args:
            websocket: WebSocket连接
            device_id: 设备ID
            
        Returns:
            bool: 连接是否成功
        """
        if device_id not in self.active_connections:
            self.active_connections[device_id] = set()
        
        # 记录连接
        self.active_connections[device_id].add(websocket)
        
        # 更新状态
        self.device_status[device_id] = "online"
        current_time = time.time()
        self.connection_times[device_id] = current_time
        self.last_activity[device_id] = current_time
        
        logger.info(f"设备 {device_id} 已连接, 当前连接数: {len(self.active_connections[device_id])}")
        return True
    
    async def disconnect(self, websocket: websockets.WebSocketServerProtocol, device_id: str):
        """
        断开连接
        
        Args:
            websocket: WebSocket连接
            device_id: 设备ID
        """
        if device_id in self.active_connections:
            try:
                # 移除连接
                self.active_connections[device_id].remove(websocket)
                
                # 如果没有连接，更新状态为离线
                if not self.active_connections[device_id]:
                    del self.active_connections[device_id]
                    self.device_status[device_id] = "offline"
                    logger.info(f"设备 {device_id} 已断开所有连接")
                else:
                    logger.info(f"设备 {device_id} 断开一个连接，剩余连接数: {len(self.active_connections[device_id])}")
            except KeyError:
                logger.warning(f"尝试断开未记录的连接: {device_id}")
    
    async def close_connection(self, device_id: str, code: int = 1000, reason: str = "服务器主动关闭连接"):
        """
        主动关闭设备的所有连接
        
        Args:
            device_id: 设备ID
            code: 关闭代码
            reason: 关闭原因
        """
        if device_id not in self.active_connections:
            logger.warning(f"尝试关闭不存在的设备连接: {device_id}")
            return
            
        connections = list(self.active_connections[device_id])
        for websocket in connections:
            try:
                await websocket.close(code=code, reason=reason)
            except Exception as e:
                logger.error(f"关闭设备 {device_id} 连接时出错: {str(e)}")
        
        # 更新状态
        if device_id in self.active_connections:
            del self.active_connections[device_id]
        self.device_status[device_id] = "offline"
        
        logger.info(f"已主动关闭设备 {device_id} 的所有连接")
    
    async def update_activity(self, device_id: str):
        """
        更新设备最后活动时间
        
        Args:
            device_id: 设备ID
        """
        if device_id in self.active_connections:
            self.last_activity[device_id] = time.time()
    
    def is_device_online(self, device_id: str) -> bool:
        """
        检查设备是否在线
        
        Args:
            device_id: 设备ID
            
        Returns:
            bool: 是否在线
        """
        return device_id in self.active_connections and len(self.active_connections[device_id]) > 0
    
    def get_device_status(self, device_id: str) -> str:
        """
        获取设备状态
        
        Args:
            device_id: 设备ID
            
        Returns:
            str: 设备状态
        """
        return self.device_status.get(device_id, "unknown")
    
    def get_device_connection_time(self, device_id: str) -> Optional[datetime]:
        """
        获取设备连接时间
        
        Args:
            device_id: 设备ID
            
        Returns:
            Optional[datetime]: 连接时间，如果不存在则返回None
        """
        timestamp = self.connection_times.get(device_id)
        if timestamp:
            return datetime.fromtimestamp(timestamp)
        return None
    
    def get_device_last_activity(self, device_id: str) -> Optional[datetime]:
        """
        获取设备最后活动时间
        
        Args:
            device_id: 设备ID
            
        Returns:
            Optional[datetime]: 最后活动时间，如果不存在则返回None
        """
        timestamp = self.last_activity.get(device_id)
        if timestamp:
            return datetime.fromtimestamp(timestamp)
        return None
    
    def get_online_devices(self) -> Dict[str, Dict]:
        """
        获取所有在线设备信息
        
        Returns:
            Dict[str, Dict]: 设备ID到设备信息的映射
        """
        result = {}
        for device_id in self.active_connections:
            if len(self.active_connections[device_id]) > 0:
                result[device_id] = {
                    "status": "online",
                    "connections": len(self.active_connections[device_id]),
                    "connected_at": self.get_device_connection_time(device_id),
                    "last_activity": self.get_device_last_activity(device_id)
                }
        return result
    
    async def broadcast(self, message: str, exclude_device_id: Optional[str] = None):
        """
        向所有设备广播消息
        
        Args:
            message: 要广播的消息
            exclude_device_id: 要排除的设备ID
        """
        disconnected_devices = []
        
        for device_id, connections in self.active_connections.items():
            if exclude_device_id and device_id == exclude_device_id:
                continue
                
            for websocket in connections:
                try:
                    await websocket.send(message)
                except Exception as e:
                    logger.error(f"向设备 {device_id} 发送消息失败: {str(e)}")
                    
                    # 收集断开的连接
                    if device_id not in disconnected_devices:
                        disconnected_devices.append((device_id, websocket))
        
        # 清理断开的连接
        for device_id, websocket in disconnected_devices:
            await self.disconnect(websocket, device_id)

# 全局连接管理器实例
connection_manager = ConnectionManager() 