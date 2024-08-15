package animores.api_gateway.exception;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.handler.ResponseStatusExceptionHandler;
import reactor.core.publisher.Mono;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseStatusExceptionHandler {

    @ExceptionHandler(ExpiredJwtException.class)
    public Mono<ResponseEntity<String>> handleExpiredJwtException(ExpiredJwtException e) {
        return Mono.just(ResponseEntity.status(401).body("Token is expired"));
    }
}
