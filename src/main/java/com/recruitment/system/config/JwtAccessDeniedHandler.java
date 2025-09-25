package com.recruitment.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler cho xử lý lỗi access denied (403 Forbidden)
 */
@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, 
                      HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.error("Access denied error: {}", accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        final Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Forbidden: " + accessDeniedException.getMessage());
        body.put("error", "Forbidden");
        body.put("path", request.getServletPath());

        final ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
