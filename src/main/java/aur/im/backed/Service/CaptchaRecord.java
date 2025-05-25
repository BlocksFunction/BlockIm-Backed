package aur.im.backed.Service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CaptchaRecord {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private final HashOperations<String, String, String> hashOperations;

	@Autowired
	public CaptchaRecord(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.hashOperations = redisTemplate.opsForHash();
	}

	public void addCaptchaRecord(String ip, String verifyCode) {
		hashOperations.put("captchaRecord", ip, verifyCode);
		redisTemplate.expire("captchaRecord", 2, TimeUnit.MINUTES);
	}

	public String getRequestCount(String ip) {
		return hashOperations.get("captchaRecord", ip);
	}

	public Boolean hasCaptcha(String ip) {
		return hashOperations.hasKey("captchaRecord", ip);
	}

	public void deleteRequestCount(String ip) {
		hashOperations.delete("captchaRecord", ip);
	}
}
