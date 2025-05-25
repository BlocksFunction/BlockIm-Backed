package aur.im.backed.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserLoginInfoService {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private final HashOperations<String, String, String> hashOperations;

	@Autowired
	public UserLoginInfoService(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.hashOperations = redisTemplate.opsForHash();
	}

	/**
	 * 保存用户登录信息到 Redis 哈希表中
	 *
	 * @param username 用户名，作为哈希表的键
	 * @param clientId 客户端 ID，作为哈希表中的字段
	 * @param token    令牌，作为哈希表中的值
	 */
	public void saveUserLoginInfo(
		String username,
		String clientId,
		String token
	) {
		hashOperations.put("userLoginInfo:" + username, clientId, token);
	}

	/**
	 * 批量保存用户登录信息
	 *
	 * @param username 用户名，作为哈希表的键
	 * @param clientIdsAndTokens 包含多个 clientId 和 token 的映射
	 */
	public void saveUserLoginInfos(
		String username,
		Map<String, String> clientIdsAndTokens
	) {
		hashOperations.putAll("userLoginInfo:" + username, clientIdsAndTokens);
	}

	/**
	 * 获取存储在 Redis 哈希表中的用户登录信息
	 *
	 * @param username 用户名，作为哈希表的键
	 * @return 包含用户登录信息的 Map（clientId -> token）
	 */
	public Map<String, String> getUserLoginInfo(String username) {
		return hashOperations.entries("userLoginInfo:" + username);
	}

	/**
	 * 根据 clientId 获取 token
	 *
	 * @param username 用户名，作为哈希表的键
	 * @param clientId 客户端 ID，作为哈希表中的字段
	 * @return 对应的 token
	 */
	public String getTokenByUsernameAndClientId(
		String username,
		String clientId
	) {
		return hashOperations.get("userLoginInfo:" + username, clientId);
	}

	/**
	 * 删除特定 clientId 的 token
	 *
	 * @param username 用户名，作为哈希表的键
	 * @param clientId 客户端 ID，作为哈希表中的字段
	 */
	public void deleteUserLoginInfo(String username, String clientId) {
		hashOperations.delete("userLoginInfo:" + username, clientId);
	}

	/**
	 * 设置哈希表的过期时间
	 *
	 * @param username 用户名，作为哈希表的键
	 * @param seconds  过期时间（秒）
	 */
	public void setUserLoginInfoExpiration(String username, long seconds) {
		redisTemplate.expire(
			"userLoginInfo:" + username,
			seconds,
			TimeUnit.SECONDS
		);
	}
}
