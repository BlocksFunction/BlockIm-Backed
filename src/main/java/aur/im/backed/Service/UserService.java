package aur.im.backed.Service;

import aur.im.backed.Entry.Users;
import aur.im.backed.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 用户服务类，提供用户相关的业务逻辑。
 *
 * <p>该类负责处理用户相关的操作，如保存用户信息、获取用户密码哈希值等。
 * 它依赖于 {@link UserRepository} 来执行数据库操作。
 *
 * @see UserRepository
 * @see Users
 */
@Service
public class UserService {

	/**
	 * 用户仓库，用于执行数据库操作。
	 */
	private final UserRepository userRepository;

	/**
	 * 构造函数，通过依赖注入初始化用户服务。
	 *
	 * @param userRepository 用户仓库
	 */
	@Autowired
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	/**
	 * 保存用户信息。
	 *
	 * <p>该方法首先检查用户名是否已存在，如果存在则抛出异常。
	 * 如果用户名不存在，则将用户信息保存到数据库中。
	 *
	 * @param user 要保存的用户对象
	 * @throws RuntimeException 如果用户名已存在
	 */
	public void saveUser(Users user) {
		if (userRepository.existsByUsername(user.getUsername())) {
			throw new RuntimeException("该用户已存在");
		}
		userRepository.save(user);
	}

	/**
	 * 根据用户名查找用户。
	 *
	 * @param username 用户名
	 * @return 返回找到的用户
	 * @throws RuntimeException 如果找不到用户，则抛出异常
	 */
	public Users getUsersByUsername(String username) {
		Users user = userRepository.findByUsername(username);
		if (user == null) {
			throw new RuntimeException("用户未找到");
		}
		return user;
	}

	/**
	 * 根据用户名查找用户。
	 *
	 * @param userId 用户名
	 * @return 返回找到的用户
	 */
	public Users getUsersByUserId(Long userId) {
		return userRepository.findByUserId(userId);
	}

	/**
	 * 根据邮箱地址查找用户。
	 *
	 * @param email 邮箱地址
	 * @return 返回找到的用户
	 */
	public Users getUsersByEmail(String email) {
		return userRepository.findByEmail(email);
	}
}
