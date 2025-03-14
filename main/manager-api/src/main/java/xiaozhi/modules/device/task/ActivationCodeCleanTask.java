package xiaozhi.modules.device.task;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import xiaozhi.modules.device.service.ActivationCodeService;

/**
 * 激活码清理定时任务
 */
@Component
@AllArgsConstructor
@Slf4j
public class ActivationCodeCleanTask {

    private final ActivationCodeService activationCodeService;

    /**
     * 每小时执行一次，清理过期激活码
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void cleanExpiredCodes() {
        log.info("开始执行过期激活码清理任务");
        activationCodeService.cleanExpiredCodes();
        log.info("过期激活码清理任务执行完毕");
    }
} 