package com.example.agenticrag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final OpenRouterClient openRouterClient;

    public ChatController(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new IllegalArgumentException("question must not be blank");
        }
        return new ChatResponse(openRouterClient.answer(request.question()));
    }

    public record ChatRequest(String question) {
    }

    public record ChatResponse(String answer) {
    }
}

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> badRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<ApiError> serviceUnavailable(IllegalStateException exception) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiError(exception.getMessage()));
    }

    @ExceptionHandler(OpenRouterClient.OpenRouterException.class)
    ResponseEntity<ApiError> openRouterError(OpenRouterClient.OpenRouterException exception) {
        HttpStatus status = switch (exception.getStatusCode()) {
            case 402 -> HttpStatus.PAYMENT_REQUIRED;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            default -> HttpStatus.BAD_GATEWAY;
        };
        return ResponseEntity.status(status).body(new ApiError(exception.getMessage()));
    }

    public record ApiError(String error) {
    }
}
