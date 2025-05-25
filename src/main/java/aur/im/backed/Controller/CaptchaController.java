package aur.im.backed.Controller;

import static aur.im.backed.Utils.BackResponseUtils.SuccessResponseJson;

import aur.im.backed.Service.CaptchaRecord;
import aur.im.backed.Utils.IPUtils;
import cn.hutool.core.util.RandomUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/captcha")
public class CaptchaController {

	@Autowired
	private CaptchaRecord captchaRecord;

	/**
	 * 处理验证码获取请求
	 *
	 * <p>根据客户端IP地址进行验证码管理：
	 * <ul>
	 *   <li>当IP未存在有效验证码时：生成4位随机验证码并记录，响应中包含新验证码字符串</li>
	 *   <li>当IP已存在有效验证码时：返回该IP当前的验证码请求次数</li>
	 * </ul>
	 *
	 * @param request HTTP请求对象，用于通过{@link IPUtils}提取客户端真实IP
	 * @return 响应实体包含：
	 *         <ul>
	 *           <li>新验证码生成：{"code": "4位随机字母数字字符串"}</li>
	 *           <li>已存在验证码：{"code": 当前请求次数（整型）}</li>
	 *         </ul>
	 * @see IPUtils#getClientIp(HttpServletRequest) 具体IP提取实现
	 * @see CaptchaRecord 验证码存储管理实现
	 */
	@GetMapping("/getCaptcha")
	public ResponseEntity<Map<String, Object>> getCaptcha(
		HttpServletRequest request
	) {
		IPUtils ipUtils = new IPUtils();
		String ip = ipUtils.getClientIp(request);

		if (captchaRecord.hasCaptcha(ip)) {
			return SuccessResponseJson(
				Map.of("code", captchaRecord.getRequestCount(ip))
			);
		} else {
			String verifyCode = RandomUtil.randomString(4);
			captchaRecord.addCaptchaRecord(ip, verifyCode);
			return SuccessResponseJson(Map.of("code", verifyCode));
		}
	}
}
