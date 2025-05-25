package aur.im.backed.Utils;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class BackResponseUtils {

	/**
	 * 生成标准化成功 JSON 响应的 {@link ResponseEntity} 对象
	 * <p>
	 * 该方法用于快速构建 REST API 成功响应，响应体将包含：
	 * <ul>
	 *   <li>{@code status} - 固定为 "success" 字符串</li>
	 *   <li>用户提供的 {@code data} 中的所有键值对</li>
	 * </ul>
	 *
	 * @param data 要包含在响应体中的有效数据，应为非空 Map（允许空值条目，
	 *             但至少需要包含一个顶层键）。典型数据如：
	 *             <pre>{@code
	 *             Map.of("user", userDTO, "metadata", pagingInfo)
	 *             }</pre>
	 * @return HTTP 状态码 200 (OK) 的响应实体，响应体结构示例：
	 *         <pre>{@code
	 *         {
	 *           "status": "success",
	 *           "token": "abc.def.ghi",
	 *           "clientId": "xyz123"
	 *         }
	 *         }</pre>
	 * @throws IllegalArgumentException 如果 data 参数为 null
	 * @see #ErrorResponseJson(String, HttpStatus)
	 */
	public static ResponseEntity<Map<String, Object>> SuccessResponseJson(
		Map<String, Object> data
	) {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "success");
		response.putAll(data);
		return ResponseEntity.ok(response);
	}

	/**
	 * 生成标准化错误 JSON 响应的 {@link ResponseEntity} 对象
	 * <p>
	 * 用于构建错误响应，包含机器可读的错误标识和人类可读的错误说明。响应体结构：
	 * <ul>
	 *   <li>{@code status} - 固定为 "error" 字符串</li>
	 *   <li>{@code reason} - 错误描述信息（应避免暴露敏感信息）</li>
	 * </ul>
	 *
	 * @param reason  简明错误描述，推荐使用预定义的错误代码短语（如："invalid_credentials"），
	 *                而不是直接面向终端用户的文字。长度建议控制在 200 字符以内
	 * @param status  符合 REST 语义的 HTTP 状态码，通常为 4xx 或 5xx 系列状态码，
	 *                例如 {@link HttpStatus#BAD_REQUEST}、{@link HttpStatus#FORBIDDEN}
	 * @return 包含指定状态码的错误响应实体，响应体示例：
	 *         <pre>{@code
	 *         {
	 *           "status": "error",
	 *           "reason": "<错误原因>"
	 *         }
	 *         }</pre>
	 * @throws IllegalArgumentException 如果 reason 为 null 或空字符串，或 status 为 2xx 系列状态码
	 * @see <a href="https://httpstatuses.com">HTTP 状态码规范</a>
	 */
	public static ResponseEntity<Map<String, Object>> ErrorResponseJson(
		String reason,
		HttpStatus status
	) {
		Map<String, Object> response = new HashMap<>();
		response.put("status", "error");
		response.put("reason", reason);
		return ResponseEntity.status(status).body(response);
	}

	/**
	 * 生成包含任意格式图片数据的成功响应实体
	 * <p>
	 * 该方法用于返回二进制图片流的成功响应场景，需显式指定图片格式。自动配置以下响应头：
	 * <ul>
	 *   <li>{@code Content-Type} - 根据参数指定的媒体类型（如：image/png, image/jpeg）</li>
	 *   <li>{@code Content-Length} - 自动计算并设置图片字节流长度</li>
	 * </ul>
	 *
	 * @param photo      要返回的图片二进制数据，必须满足：
	 *                   <ul>
	 *                     <li>非空数组（长度 > 0）</li>
	 *                     <li>有效的图片编码数据</li>
	 *                   </ul>
	 * @param mediaType  图片的媒体类型，定义响应内容的格式，常用值包括：
	 *                   <ul>
	 *                     <li>{@link MediaType#IMAGE_PNG}</li>
	 *                     <li>{@link MediaType#IMAGE_JPEG}</li>
	 *                     <li>{@link MediaType#IMAGE_GIF}</li>
	 *                   </ul>
	 * @return HTTP 状态码 200 (OK) 的响应实体，响应体为原始图片字节流。
	 *         使用示例：
	 *         <pre>{@code
	 *         byte[] imageData = Files.readAllBytes(Paths.get("photo.png"));
	 *         return SuccessResponsePhoto(imageData, MediaType.IMAGE_PNG);
	 *         }</pre>
	 * @throws IllegalArgumentException 如果出现以下情况：
	 *                                  <ul>
	 *                                    <li>photo 为 null 或空数组</li>
	 *                                    <li>mediaType 为 null</li>
	 *                                    <li>mediaType 不属于 image/* 类型</li>
	 *                                  </ul>
	 * @see org.springframework.http.MediaType
	 */
	public static ResponseEntity<byte[]> SuccessResponsePhoto(
		byte[] photo,
		MediaType mediaType
	) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(mediaType);
		headers.setContentLength(photo.length);
		return ResponseEntity.ok().headers(headers).body(photo);
	}

	/**
	 * 生成图片请求的错误响应实体（空内容体）
	 * <p>
	 * 用于处理图片资源请求失败场景，返回符合 REST 规范的状态码。注意：
	 * <ul>
	 *   <li>响应体内容为空（null），客户端应根据状态码处理错误</li>
	 *   <li>建议配合 {@code Accept} 请求头，在需要时返回结构化错误信息</li>
	 *   <li>生产环境建议在网关层统一替换为默认错误图片</li>
	 * </ul>
	 *
	 * @param status 错误状态码，必须为 4xx 或 5xx 系列状态码，
	 *               典型值包括：
	 *               <ul>
	 *                 <li>{@link HttpStatus#NOT_FOUND} 404 - 图片资源不存在</li>
	 *                 <li>{@link HttpStatus#UNSUPPORTED_MEDIA_TYPE} 415 - 不支持的图片格式</li>
	 *                 <li>{@link HttpStatus#INTERNAL_SERVER_ERROR} 500 - 服务器处理异常</li>
	 *               </ul>
	 * @return 指定状态码的空内容响应实体，示例响应：
	 *         <pre>{@code
	 *         HTTP/1.1 404 Not Found
	 *         Content-Length: 0
	 *         }</pre>
	 * @throws IllegalArgumentException 如果 status 参数为 1xx 或 2xx 系列状态码
	 * @see <a href="https://httpstatuses.com">HTTP Status Code Definitions</a>
	 */
	public static ResponseEntity<byte[]> ErrorResponsePhoto(HttpStatus status) {
		return ResponseEntity.status(status).body(null);
	}
}
