package xiaozhi.modules.device.constant;

/**
 * 设备常量
 */
public class DeviceConstant {

    /**
     * 设备状态
     */
    public static class Status {
        /**
         * 未激活
         */
        public static final int INACTIVE = 0;
        
        /**
         * 等待激活
         */
        public static final int WAITING = 1;
        
        /**
         * 已激活
         */
        public static final int ACTIVE = 2;
    }
    
    /**
     * 激活码状态
     */
    public static class ActivationStatus {
        /**
         * 有效
         */
        public static final int VALID = 0;
        
        /**
         * 已使用
         */
        public static final int USED = 1;
        
        /**
         * 已过期
         */
        public static final int EXPIRED = 2;
    }
    
    /**
     * 令牌是否撤销
     */
    public static class TokenRevoked {
        /**
         * 否
         */
        public static final int NO = 0;
        
        /**
         * 是
         */
        public static final int YES = 1;
    }
    
    /**
     * 激活码有效期（分钟）
     */
    public static final int DEFAULT_ACTIVATION_EXPIRE_MINUTES = 30;
    
    /**
     * 访问令牌有效期（小时）
     */
    public static final int DEFAULT_TOKEN_EXPIRE_HOURS = 24 * 7;  // 一周
} 