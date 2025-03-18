package xiaozhi.common.api;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * 标准API响应格式
 * 
 * @param <T> 响应数据类型
 */
@Data
public class ApiResponse<T> {
    private boolean success;
    private int status;
    private String message;
    private T data;
    
    /**
     * 创建成功响应
     * 
     * @param data 响应数据
     * @return 标准响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("操作成功");
        response.setData(data);
        return response;
    }
    
    /**
     * 创建带自定义消息的成功响应
     * 
     * @param message 成功消息
     * @param data 响应数据
     * @return 标准响应对象
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 创建错误响应
     * 
     * @param message 错误消息
     * @return 标准响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        response.setMessage(message);
        return response;
    }
    
    /**
     * 创建带状态码的错误响应
     * 
     * @param status HTTP状态码
     * @param message 错误消息
     * @return 标准响应对象
     */
    public static <T> ApiResponse<T> error(int status, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setStatus(status);
        response.setMessage(message);
        return response;
    }
} 