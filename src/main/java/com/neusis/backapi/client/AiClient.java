package com.neusis.backapi.client;

import com.neusis.backapi.dto.AiAnalysisDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class AiClient {

    private final WebClient webClient;

    public AiClient(@Value("${ai.base-url:http://localhost:8001}") String aiBaseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(aiBaseUrl)
                .build();
    }

    public AiAnalysisDto.Response analyze(AiAnalysisDto.Request request) {
        return webClient.post()
                .uri("/analyze")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AiAnalysisDto.Response.class)
                .block();
    }
}