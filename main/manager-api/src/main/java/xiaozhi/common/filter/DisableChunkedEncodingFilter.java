package xiaozhi.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

/**
 * 禁用分块传输编码的过滤器
 * 通过包装响应对象，在响应发送前计算并设置Content-Length头
 */
@Component
public class DisableChunkedEncodingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 对于API请求，强制使用Content-Length
        if (request.getRequestURI().contains("/xiaozhi/ota")) {
            ContentLengthResponseWrapper responseWrapper = new ContentLengthResponseWrapper(response);
            filterChain.doFilter(request, responseWrapper);
            responseWrapper.flushBuffer();
        } else {
            // 其他请求正常处理
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 内部类：响应包装器，计算内容长度
     */
    private static class ContentLengthResponseWrapper extends HttpServletResponseWrapper {
        private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        private PrintWriter printWriter;

        public ContentLengthResponseWrapper(HttpServletResponse response) {
            super(response);
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    byteArrayOutputStream.write(b);
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setWriteListener(WriteListener writeListener) {
                    // 不需要实现
                }
            };
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (printWriter == null) {
                printWriter = new PrintWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8));
            }
            return printWriter;
        }

        @Override
        public void flushBuffer() throws IOException {
            if (printWriter != null) {
                printWriter.flush();
            }
            
            byte[] bytes = byteArrayOutputStream.toByteArray();
            
            // 设置内容长度，显式禁用分块传输
            HttpServletResponse httpResponse = (HttpServletResponse) getResponse();
            httpResponse.setHeader("Transfer-Encoding", "identity");
            httpResponse.setContentLength(bytes.length);
            
            // 写入实际内容
            getResponse().getOutputStream().write(bytes);
            getResponse().getOutputStream().flush();
        }
    }
} 