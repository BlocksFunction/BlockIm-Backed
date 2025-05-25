package aur.im.backed.Utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

	//	private static final Key SECRET_KEY = Keys.secretKeyFor(
	//		SignatureAlgorithm.HS256
	//	);
	private static final String SECRET_STRING =
		"this-is-a-very-long-secret-key-with-32-chars-1234567890";
	private static final Key SECRET_KEY = Keys.hmacShaKeyFor(
		SECRET_STRING.getBytes(StandardCharsets.UTF_8)
	);
	private static final long EXPIRATION_TIME = 30L * 24 * 60 * 60 * 1000;

	public static String generateToken(String username, String userId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userId);
		claims.put("jti", UUID.randomUUID().toString());

		return Jwts.builder()
			.setClaims(claims)
			.setSubject(username)
			.setIssuedAt(new Date())
			.setExpiration(
				new Date(System.currentTimeMillis() + EXPIRATION_TIME)
			)
			.signWith(SECRET_KEY, SignatureAlgorithm.HS256)
			.compact();
	}

	public static Claims validateToken(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(SECRET_KEY)
			.build()
			.parseClaimsJws(token)
			.getBody();
	}

	public static String getUsernameFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(SECRET_KEY)
			.build()
			.parseClaimsJws(token)
			.getBody();

		return claims.getSubject();
	}

	public static void revokeToken(String token) {}
}
