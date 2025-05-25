package aur.im.backed.Service;

import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CountOfAccountTypeOperationRequestsService {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private final HashOperations<String, String, String> hashOperations;

	@Autowired
	public CountOfAccountTypeOperationRequestsService(
		RedisTemplate<String, Object> redisTemplate
	) {
		this.redisTemplate = redisTemplate;
		this.hashOperations = redisTemplate.opsForHash();
	}

	public void incrementRequestCount(String ip) {
		String currentValue = hashOperations.get(
			"countOfAccountTypeOperationRequests",
			ip
		);
		int count = (currentValue != null) ? Integer.parseInt(currentValue) : 0;
		hashOperations.put(
			"countOfAccountTypeOperationRequests",
			ip,
			String.valueOf(count + 1)
		);
		redisTemplate.expire(
			"countOfAccountTypeOperationRequests",
			2,
			TimeUnit.MINUTES
		);
	}

	public String getRequestCount(String ip) {
		return hashOperations.get("countOfAccountTypeOperationRequests", ip);
	}

	public void deleteRequestCount(String ip) {
		hashOperations.delete("countOfAccountTypeOperationRequests", ip);
	}
}
