package ru.practicum.core.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.EndpointHitDto;

import java.time.LocalDateTime;

@Component
@Slf4j
public class GlobalInterceptor implements HandlerInterceptor {

    @Value("${EWMServiceApp.name}")
    private String appName;

    private final StatsClient statsClient;

    @Autowired
    public GlobalInterceptor(StatsClient statsClient) {
        this.statsClient = statsClient;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        try {
            ResponseEntity<Object> statsResponse = statsClient.save(EndpointHitDto.builder()
                    .app(appName)
                    .uri(request.getRequestURI())
                    .ip(request.getRemoteAddr())
                    .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                    .build());
            if (!statsResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Ошибка при сохранении статистики: {}", statsResponse.getBody());
            }
        } catch (RuntimeException e) {
            log.error("Исключительная ситуация при сохранении статистики: {}", e.getMessage());
        }
        return true;
    }
}
