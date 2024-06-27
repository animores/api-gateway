package animores.api_gateway.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static animores.api_gateway.common.RequestConstants.ACCOUNT_ID;
import static animores.api_gateway.common.RequestConstants.ACCOUNT_ROLE;


@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {
    private final TokenProvider tokenProvider;
    private final ReactiveValueOperations<String, BlackListToken> blackListTokenOps;
    public AuthorizationHeaderFilter(TokenProvider tokenProvider, ReactiveValueOperations<String, BlackListToken> blackListTokenOps) {
        super(Config.class);
        this.tokenProvider = tokenProvider;
        this.blackListTokenOps = blackListTokenOps;
    }
    public static class Config {}

    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String token = parseBearerToken(request);

            if (token == null) {
                return continueWithAnonymous(exchange, chain);
            }

            return blackListTokenOps.get(token)
                    .flatMap(t -> continueWithAnonymous(exchange, chain))
                    .switchIfEmpty(
                            Mono.defer(() -> {
                                String[] split = Optional.of(token)
                                        .filter(subject -> subject.length() >= 10)
                                        .map(tokenProvider::validateTokenAndGetSubject)
                                        .orElse("anonymous:anonymous")
                                        .split(":");

                                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                        .header(ACCOUNT_ID, split[0])
                                        .header(ACCOUNT_ROLE, split[1])
                                        .build();

                                ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
                                return chain.filter(mutatedExchange);
                            })
                    );
        };
    }

    private Mono<Void> continueWithAnonymous(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(ACCOUNT_ID, "anonymous")
                .header(ACCOUNT_ROLE, "anonymous")
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();
        return chain.filter(mutatedExchange);
    }

    private String parseBearerToken(ServerHttpRequest request) {
        return Optional.ofNullable(request.getHeaders().get(HttpHeaders.AUTHORIZATION))
                .map(authorization -> authorization.get(0))
                .filter(token -> token.substring(0, 7).equalsIgnoreCase("Bearer "))
                .map(token -> token.substring(7))
                .orElse(null);
    }
}
