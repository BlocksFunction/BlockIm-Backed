package aur.im.backed.Controller;

import static aur.im.backed.Utils.BackResponseUtils.*;
import static aur.im.backed.Utils.ImageUtils.convertToWebP;
import static aur.im.backed.Utils.ImageUtils.getImageFormat;
import static aur.im.backed.Utils.JwtUtil.getUsernameFromToken;
import static aur.im.backed.Utils.JwtUtil.validateToken;

import aur.im.backed.Entry.Users;
import aur.im.backed.Service.UserLoginInfoService;
import aur.im.backed.Service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST 控制器类，用于处理用户头像的获取请求。
 * 该控制器提供基于用户 ID 获取头像图像的端点，并返回格式化的 HTTP 响应。
 * 头像图像的存储路径由类常量 {@value #AVATAR_PATH} 定义。
 */
@RestController
@RequestMapping("/avatar")
public class AvatarsController {

	private static final String AVATAR_PATH = "/home/imaur/桌面/";

	@Autowired
	private UserLoginInfoService userLoginInfoService;

	@Autowired
	private UserService userService;

	/**
	 * 系统支持的图片文件扩展名白名单
	 * <p>
	 * 该列表定义允许作为用户头像的有效图片格式，遵循以下规范：
	 * <ul>
	 *   <li>使用小写字母形式（文件匹配时自动忽略大小写）</li>
	 *   <li>按常用格式优先级排序（png > jpg > jpeg > gif > bmp）</li>
	 *   <li>不包含前导点（如使用 "png" 而非 ".png"）</li>
	 * </ul>
	 *
	 * <strong>安全须知：</strong>
	 * <ul>
	 *   <li>修改此列表将影响 {@link #findAvatar(String)} 等依赖此白名单的方法行为</li>
	 *   <li>新增格式需确保服务端已配置对应的 MIME 类型支持（如：image/webp）</li>
	 * </ul>
	 *
	 * 当前支持的具体格式：
	 * <ol>
	 *   <li>PNG（Portable Network Graphics）</li>
	 *   <li>JPEG（Joint Photographic Experts Group）</li>
	 *   <li>GIF（Graphics Interchange Format）</li>
	 *   <li>BMP（Bitmap）</li>
	 * </ol>
	 *
	 * @see #findAvatar(String) 头像查找方法
	 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/Media/Formats/Image_types">
	 *      常见图片格式类型参考
	 *     </a>
	 */
	private static final List<String> IMAGE_EXTENSIONS = List.of(
		"png",
		"jpg",
		"jpeg",
		"gif",
		"bmp",
		"webp"
	);

	/**
	 * 根据用户ID查找对应的头像文件及其媒体类型（支持多格式）
	 * <p>
	 * 该方法按照以下规则查找用户头像：
	 * <ol>
	 *   <li>优先查找{@code .png}格式的图片文件</li>
	 *   <li>若未找到，依次尝试其他支持的格式：{@code .jpg > .jpeg > .gif > .bmp}</li>
	 * </ol>
	 * 所有文件查找路径基于{@code AVATAR_PATH}常量指定的基础目录。
	 * </p>
	 *
	 * @param userId 用户唯一标识符，应当满足：
	 *               <ul>
	 *                 <li>非空字符串</li>
	 *                 <li>仅包含字母、数字、下划线和连字符（通过正则校验）</li>
	 *                 <li>示例："114514" 或 "user-001"</li>
	 *               </ul>
	 * @return 包含文件对象和媒体类型的 Map.Entry，可能为：
	 *         <ul>
	 *           <li>非空 Entry - 找到有效头像文件时（key=File，value=MediaType）</li>
	 *           <li>{@code null} - 未找到任何支持格式的头像文件时</li>
	 *         </ul>
	 * @throws SecurityException 如果存在文件系统访问权限问题（由{@link File#exists()}抛出）
	 * @throws IllegalArgumentException 如果userId包含非法字符
	 * @see #IMAGE_EXTENSIONS
	 */
	public static Map.Entry<File, MediaType> findAvatar(String userId) {
		if (!userId.matches("^[a-zA-Z0-9_-]+$")) {
			throw new IllegalArgumentException("INVALID_USERID_FORMAT");
		}

		File avatarFile = new File(AVATAR_PATH + userId + ".png");
		if (avatarFile.exists()) {
			return Map.entry(avatarFile, MediaType.IMAGE_PNG);
		}

		for (String ext : IMAGE_EXTENSIONS) {
			if (ext.equals("png")) continue;

			avatarFile = new File(AVATAR_PATH + userId + "." + ext);
			if (avatarFile.exists()) {
				MediaType mediaType =
					switch (ext.toLowerCase()) {
						case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
						case "gif" -> MediaType.IMAGE_GIF;
						case "bmp" -> MediaType.valueOf("image/bmp");
						case "webp" -> MediaType.valueOf("image/webp");
						default -> MediaType.APPLICATION_OCTET_STREAM;
					};
				return Map.entry(avatarFile, mediaType);
			}
		}

		return null;
	}

	/**
	 * 根据用户ID获取头像图片数据（PNG格式）
	 * <p>
	 *
	 * @param userId 用户唯一标识符，应符合以下规范：
	 *               <ul>
	 *                 <li>非空字符串</li>
	 *                 <li>仅包含安全文件名字符（字母、数字、下划线、连字符）</li>
	 *                 <li>示例值："114514"</li>
	 *               </ul>
	 * @return 响应实体包含以下可能情况：
	 *         <ul>
	 *           <li>HTTP 200 OK - 成功返回头像数据（Content-Type 固定为 image/webp）</li>
	 *           <li>HTTP 404 Not Found - 指定用户的PNG头像文件不存在</li>
	 *           <li>HTTP 500 Internal Server Error - 文件读取失败</li>
	 *         </ul>
	 * @throws IllegalArgumentException 如果 userId 包含非法路径字符（需在调用前校验）
	 *
	 */
	@GetMapping("/get/{userId}")
	public ResponseEntity<byte[]> getAvatarByUserid(
		@PathVariable String userId
	) {
		try {
			Map.Entry<File, MediaType> avatarEntry = findAvatar(userId);
			if (avatarEntry != null) {
				File avatarFile = avatarEntry.getKey();
				MediaType mediaType = avatarEntry.getValue();
				byte[] avatarBytes = Files.readAllBytes(avatarFile.toPath());
				byte[] avatarBytesWebP = convertToWebP(
					avatarBytes,
					mediaType.getSubtype(),
					0.8f
				);
				return SuccessResponsePhoto(
					avatarBytesWebP,
					MediaType.valueOf("image/webp")
				);
			} else {
				return ErrorResponsePhoto(HttpStatus.NOT_FOUND);
			}
		} catch (IllegalArgumentException | IOException e) {
			return ErrorResponsePhoto(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/upload")
	public ResponseEntity<Map<String, Object>> uploadAvatar(
		@RequestPart("avatar") MultipartFile file,
		@CookieValue("token") String token,
		@CookieValue("clientId") String clientId
	) {
		if (
			file.isEmpty() || token == null || clientId == null
		) return ErrorResponseJson(
			"文件/令牌/客户端标识码不可为空!",
			HttpStatus.BAD_REQUEST
		);
		try {
			validateToken(token);
		} catch (SignatureException | IllegalArgumentException e) {
			return ErrorResponseJson(
				"INVALID_SIGNATURE" + e.getMessage(),
				HttpStatus.BAD_REQUEST
			);
		} catch (ExpiredJwtException e) {
			return ErrorResponseJson("TOKEN_EXPIRED", HttpStatus.BAD_REQUEST);
		} catch (MalformedJwtException | UnsupportedJwtException e) {
			return ErrorResponseJson("INVALID_TOKEN", HttpStatus.BAD_REQUEST);
		}
		String username = getUsernameFromToken(token);
		if (
			!Objects.equals(
				userLoginInfoService.getTokenByUsernameAndClientId(
					username,
					clientId
				),
				token
			)
		) return ErrorResponseJson("错误的令牌", HttpStatus.BAD_REQUEST);
		//		Map<String, String> userInfo = userLoginInfoService.getUserLoginInfo(getUsernameFromToken(token));
		Users user = userService.getUsersByUsername(username);
		try {
			Map.Entry<File, MediaType> avatarEntry = findAvatar(
				user.getUserId().toString()
			);
			if (avatarEntry != null && avatarEntry.getKey() != null) {
				if (!avatarEntry.getKey().delete()) {
					return ErrorResponseJson("无法设置头像, 请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR);
				};
			}
			byte[] originBytes = file.getBytes();
			MediaType mediaType = getImageFormat(originBytes);
			if (mediaType == null) return ErrorResponseJson(
				"图像格式不正确",
				HttpStatus.BAD_REQUEST
			);
			if (
				!mediaType.equals(MediaType.valueOf("image/webp"))
			) originBytes = convertToWebP(
				originBytes,
				mediaType.getSubtype(),
				0.8f
			);
			File avatarFile = new File(
				AVATAR_PATH + user.getUserId() + ".webp"
			);
			FileOutputStream fos = new FileOutputStream(avatarFile);
			fos.write(originBytes, 0, originBytes.length);
			fos.flush();
			fos.close();
			return SuccessResponseJson(
				Map.of(
					"url",
					"http://localhost:8080/avatar/get/" +
					user.getUserId().toString()
				)
			);
		} catch (IOException e) {
			return ErrorResponseJson("无法设置头像, 请稍后再试", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
