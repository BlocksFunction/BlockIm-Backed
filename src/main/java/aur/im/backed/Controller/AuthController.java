package aur.im.backed.Controller;

import static aur.im.backed.Utils.BackResponseUtils.ErrorResponseJson;
import static aur.im.backed.Utils.BackResponseUtils.SuccessResponseJson;

import aur.im.backed.Entry.Users;
import aur.im.backed.Service.CountOfAccountTypeOperationRequestsService;
import aur.im.backed.Service.UserLoginInfoService;
import aur.im.backed.Service.UserService;
import aur.im.backed.Utils.JwtUtil;
import cn.hutool.core.util.RandomUtil;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

	private static final Argon2 ARGON2 = Argon2Factory.create(
		Argon2Factory.Argon2Types.ARGON2id
	);
	private static final int ARGON_ITERATIONS = 3;
	private static final int ARGON_MEMORY = 10240;
	private static final int ARGON_PARALLELISM = 4;

	@Autowired
	private UserService userService;

	@Autowired
	private UserLoginInfoService userLoginInfoService;

	@Autowired
	private CountOfAccountTypeOperationRequestsService countOfAccountTypeOperationRequestsService;

	/**
	 * 处理用户登录请求
	 *
	 * @param body 包含登录参数的请求体，应包含：
	 *             - inputType: 登录标识类型（"email"或"userid"）
	 *             - input: 对应类型的登录标识（邮箱或用户ID字符串）
	 *             - password: 用户密码
	 * @return ResponseEntity包含：
	 *             成功时：HTTP 200 + {token: JWT令牌, clientId: 客户端标识}
	 *             失败时：对应的错误状态码和错误信息
	 * @throws NumberFormatException 当inputType为userid但输入内容无法转换为数字时
	 * @apiNote 密码验证使用Argon2算法，连续失败可能触发安全机制
	 */
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
		@RequestBody Map<String, Object> body
	) {
		String inputType = (String) body.get("inputType");
		String inputContent = (String) body.get("input");
		String password = (String) body.get("password");

		if (
			password == null ||
			!("email".equals(inputType) || "userid".equals(inputType)) ||
			inputContent == null
		) {
			return ErrorResponseJson(
				"请求参数格式错误",
				HttpStatus.BAD_REQUEST
			);
		}

		try {
			Users user = "userid".equals(inputType)
				? userService.getUsersByUserId(Long.parseLong(inputContent))
				: userService.getUsersByEmail(inputContent);
			if (user == null || user.getIsBanned()) {
				return ErrorResponseJson(
					"用户账户不存在或已被封禁",
					HttpStatus.UNAUTHORIZED
				);
			}
			if (
				!ARGON2.verify(user.getPasswordHash(), password.toCharArray())
			) {
				return ErrorResponseJson(
					"密码验证失败",
					HttpStatus.UNAUTHORIZED
				);
			}

			String token = JwtUtil.generateToken(
				user.getUsername(),
				user.getUserId().toString()
			);
			String clientId = RandomUtil.randomString(64);
			userLoginInfoService.saveUserLoginInfo(
				user.getUsername(),
				clientId,
				token
			);

			return SuccessResponseJson(
				Map.of("token", token, "clientId", clientId)
			);
		} catch (NumberFormatException e) {
			return ErrorResponseJson(
				"UserID格式无效，应为数字类型",
				HttpStatus.BAD_REQUEST
			);
		} catch (Exception e) {
			return ErrorResponseJson(
				"系统认证服务异常: " + e.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}

	/**
	 * 处理用户注册请求
	 *
	 * @param body 包含注册参数的请求体，应包含：
	 *             - username: 用户名（唯一标识）
	 *             - email: 用户邮箱（唯一标识）
	 *             - password: 登录密码
	 * @return ResponseEntity包含：
	 *             成功时：HTTP 200 + {token: JWT令牌, clientId: 客户端标识}
	 *             失败时：对应的错误状态码和错误信息
	 * @throws DataIntegrityViolationException 当邮箱或用户名已存在时
	 * @apiNote 密码使用Argon2算法进行哈希存储，用户ID使用雪花算法生成
	 */
	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> register(
		@RequestBody Map<String, Object> body
	) {
		String username = (String) body.get("username");
		String email = (String) body.get("email");
		String password = (String) body.get("password");

		if (username == null || password == null || email == null) {
			return ErrorResponseJson(
				"必需字段缺失: username/email/password",
				HttpStatus.BAD_REQUEST
			);
		}

		try {
			String passwordHash = ARGON2.hash(
				ARGON_ITERATIONS,
				ARGON_MEMORY,
				ARGON_PARALLELISM,
				password.toCharArray()
			);
			Long userId = cn.hutool.core.util.IdUtil.getSnowflake(
				1,
				1
			).nextId();
			Users user = new Users()
				.setUserId(userId)
				.setUsername(username)
				.setEmail(email)
				.setPasswordHash(passwordHash);
			userService.saveUser(user);

			String token = JwtUtil.generateToken(username, userId.toString());
			String clientId = RandomUtil.randomString(64);
			userLoginInfoService.saveUserLoginInfo(username, clientId, token);

			return SuccessResponseJson(
				Map.of("token", token, "clientId", clientId)
			);
		} catch (DataIntegrityViolationException e) {
			return ErrorResponseJson(
				"注册失败: 邮箱已被使用",
				HttpStatus.CONFLICT
			);
		} catch (Exception e) {
			return ErrorResponseJson(
				"注册过程异常: " + e.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR
			);
		}
	}
}
