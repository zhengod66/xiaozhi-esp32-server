package xiaozhi.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Knife4j文檔訪問控制器
 * 提供簡化的路徑訪問Knife4j文檔、Swagger UI和API文檔
 */
@Controller
@RequestMapping("/knife4j")
public class Knife4jController {

    /**
     * 跳轉到Knife4j文檔頁面
     */
    @GetMapping
    public String doc() {
        return "redirect:/doc.html";
    }

    /**
     * 跳轉到Swagger UI頁面
     */
    @GetMapping("/swagger")
    public String swagger() {
        return "redirect:/swagger-ui/index.html";
    }

    /**
     * 獲取API文檔信息
     */
    @GetMapping("/api-docs")
    public String apiDocs() {
        return "redirect:/v3/api-docs";
    }
} 