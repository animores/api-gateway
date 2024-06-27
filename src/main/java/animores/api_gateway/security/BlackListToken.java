package animores.api_gateway.security;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "blacklistToken", timeToLive = 86400)// 24 hour
public class BlackListToken {

    @Id
    private String blacklistTokenKey;// key

    private Long userId;// value


    public BlackListToken(String blacklistTokenKey, Long userId) {
        this.blacklistTokenKey = blacklistTokenKey;
        this.userId = userId;
    }

}