package xiaozhi.modules.device.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xiaozhi.common.utils.Result;
import xiaozhi.modules.device.service.AccessTokenService;

/**
 * 訪問令牌清理任務
 */
@Component
@RestController
@RequestMapping("/admin/token")
@AllArgsConstructor
@Slf4j
public class AccessTokenCleanTask {

    private final AccessTokenService accessTokenService;

    /**
     * 每月1日凌晨3點執行一次，清理過期令牌
     * 由於令牌清理不是高優先級任務，我們降低了執行頻率
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void cleanExpiredTokens() {
        log.info("開始執行過期令牌清理任務");
        int count = accessTokenService.cleanExpiredTokens();
        log.info("過期令牌清理任務執行完畢，共清理{}個過期令牌", count);
    }
    
    /**
     * 手動觸發令牌清理
     * 通過API接口：/xiaozhi-esp32-api/admin/token/clean
     */
    @GetMapping("/clean")
    public Result manualCleanExpiredTokens() {
        log.info("手動觸發過期令牌清理任務");
        int count = accessTokenService.cleanExpiredTokens();
        return new Result().ok("成功清理" + count + "個過期令牌");
    }
} 