import uvicorn
from fastapi import FastAPI, HTTPException, Depends, Header
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, List, Optional
import logging
from core.connection_manager import connection_manager
from datetime import datetime

# 配置日志
logger = logging.getLogger(__name__)

app = FastAPI(
    title="Xiaozhi ESP32 Server API",
    description="ESP32设备连接状态API",
    version="1.0.0"
)

# 配置CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 允许所有来源，生产环境应该限制
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# 数据模型
class DeviceStatus(BaseModel):
    """设备状态模型"""
    device_id: str
    status: str
    connections: Optional[int] = None
    connected_at: Optional[datetime] = None
    last_activity: Optional[datetime] = None

class DeviceStatusList(BaseModel):
    """设备状态列表模型"""
    devices: List[DeviceStatus]
    total: int
    online_count: int

# 简单的API密钥认证
API_KEYS = {"xiaozhi_api_key": "admin"}  # 示例密钥，生产环境应该更安全

def verify_api_key(x_api_key: str = Header(None)):
    """验证API密钥"""
    if x_api_key not in API_KEYS.values():
        raise HTTPException(status_code=401, detail="无效的API密钥")
    return x_api_key

@app.get("/api/devices", response_model=DeviceStatusList, tags=["devices"])
async def get_all_devices(api_key: str = Depends(verify_api_key)):
    """
    获取所有设备状态
    """
    try:
        online_devices = connection_manager.get_online_devices()
        device_list = []
        
        for device_id, info in online_devices.items():
            device_list.append(DeviceStatus(
                device_id=device_id,
                status=info["status"],
                connections=info["connections"],
                connected_at=info["connected_at"],
                last_activity=info["last_activity"]
            ))
        
        return DeviceStatusList(
            devices=device_list,
            total=len(device_list),
            online_count=len(device_list)
        )
    except Exception as e:
        logger.error(f"获取设备列表出错: {str(e)}")
        raise HTTPException(status_code=500, detail=f"服务器内部错误: {str(e)}")

@app.get("/api/devices/{device_id}", response_model=DeviceStatus, tags=["devices"])
async def get_device_status(device_id: str, api_key: str = Depends(verify_api_key)):
    """
    获取指定设备状态
    """
    try:
        status = connection_manager.get_device_status(device_id)
        
        if status == "unknown":
            raise HTTPException(status_code=404, detail=f"设备 {device_id} 不存在")
            
        if status == "online":
            # 获取连接详情
            online_devices = connection_manager.get_online_devices()
            if device_id in online_devices:
                info = online_devices[device_id]
                return DeviceStatus(
                    device_id=device_id,
                    status=status,
                    connections=info["connections"],
                    connected_at=info["connected_at"],
                    last_activity=info["last_activity"]
                )
        
        # 离线设备
        return DeviceStatus(
            device_id=device_id,
            status=status
        )
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"获取设备状态出错: {str(e)}")
        raise HTTPException(status_code=500, detail=f"服务器内部错误: {str(e)}")

@app.post("/api/devices/{device_id}/disconnect", tags=["devices"])
async def disconnect_device(device_id: str, api_key: str = Depends(verify_api_key)):
    """
    断开指定设备的连接
    """
    try:
        if not connection_manager.is_device_online(device_id):
            raise HTTPException(status_code=404, detail=f"设备 {device_id} 不在线")
            
        await connection_manager.close_connection(device_id, code=1000, reason="管理员断开连接")
        return {"message": f"设备 {device_id} 已断开连接"}
    except HTTPException:
        raise
    except Exception as e:
        logger.error(f"断开设备连接出错: {str(e)}")
        raise HTTPException(status_code=500, detail=f"服务器内部错误: {str(e)}")

@app.get("/api/health", tags=["system"])
async def health_check():
    """
    健康检查接口
    """
    return {
        "status": "running",
        "online_devices": len(connection_manager.get_online_devices())
    }

def start_api_server(host: str = "0.0.0.0", port: int = 8080):
    """
    启动API服务器
    
    Args:
        host: 主机地址
        port: 端口号
    """
    uvicorn.run(app, host=host, port=port)

if __name__ == "__main__":
    # 配置日志
    logging.basicConfig(
        level=logging.INFO,
        format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    )
    
    # 启动API服务器
    start_api_server() 