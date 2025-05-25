package aur.im.backed.Utils;

import jakarta.servlet.http.HttpServletRequest;

public class IPUtils {

	public String getClientIp(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			String[] ips = xForwardedFor.split(",");
			for (String ip : ips) {
				ip = ip.trim();
				if (isValidIp(ip)) {
					return ip;
				}
			}
		}

		String xRealIp = request.getHeader("X-Real-IP");
		if (xRealIp != null && !xRealIp.isEmpty() && isValidIp(xRealIp)) {
			return xRealIp;
		}

		return request.getRemoteAddr();
	}

	public boolean isValidIp(String ip) {
		if (ip == null || ip.isEmpty()) {
			return false;
		}
		return !ip.equalsIgnoreCase("unknown");
	}
}
