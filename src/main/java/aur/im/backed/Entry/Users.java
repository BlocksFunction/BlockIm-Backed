package aur.im.backed.Entry;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 用户实体类，表示系统中的一个用户。
 *
 * <p>该类包含用户的基本信息，如用户名、密码、昵称、是否被封禁、是否是管理员等。
 * 它还包含创建时间和最后登录时间的时间戳字段。
 *
 * @see jakarta.persistence.Entity
 */
@Entity
public class Users {

	/**
	 * 用户的唯一标识符。
	 *
	 * <p>该字段用于唯一标识一个用户，通常由数据库自动生成。
	 *
	 * @see jakarta.persistence.Id
	 */
	@Id
	private Long userId;

	/**
	 * 用户的用户名。
	 *
	 * <p>用户名是唯一的，不能为空，长度限制为 64 个字符。
	 *
	 * @see jakarta.persistence.Column
	 */
	@Column(unique = true, nullable = false, length = 64)
	private String username;

	/**
	 * 用户的邮箱。
	 *
	 * <p>邮箱是唯一的，不能为空，长度限制为 64 个字符。
	 *
	 * @see jakarta.persistence.Column
	 */
	@Column(unique = true, nullable = false, length = 255)
	private String email;

	/**
	 * 用户的密码哈希值。
	 *
	 * <p>密码哈希值不能为空，用于存储用户的加密密码。
	 *
	 * @see jakarta.persistence.Column
	 */
	@Column(nullable = false)
	private String passwordHash;

	/**
	 * 用户是否被封禁。
	 *
	 * <p>默认值为 {@code false}，表示用户未被封禁。
	 *
	 * @see jakarta.persistence.Column
	 */
	@Column(nullable = false)
	private boolean isBanned = false;

	/**
	 * 用户是否是管理员。
	 *
	 * <p>默认值为 {@code false}，表示用户不是管理员。
	 *
	 * @see jakarta.persistence.Column
	 */
	@Column(nullable = false)
	private boolean isAdmin = false;

	/**
	 * 用户的创建时间。
	 *
	 * <p>该字段由数据库自动生成，表示用户创建的时间。
	 * 它是不可更新的，一旦设置后不能修改。
	 *
	 * @see org.hibernate.annotations.CreationTimestamp
	 */
	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	/**
	 * 用户的最后登录时间。
	 *
	 * <p>该字段由数据库自动生成，表示用户最后登录的时间。
	 * 每次用户登录时会自动更新。
	 *
	 * @see org.hibernate.annotations.UpdateTimestamp
	 */
	@UpdateTimestamp
	private LocalDateTime lastLogin;

	/**
	 * 默认构造函数。
	 *
	 * <p>用于框架初始化实体对象。
	 */
	public Users() {}

	/**
	 * 获取用户的唯一标识符。
	 *
	 * @return 用户的唯一标识符
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * 设置用户的唯一标识符。
	 *
	 * @param userId 用户的唯一标识符
	 * @return 当前对象，用于链式调用
	 */
	public Users setUserId(Long userId) {
		this.userId = userId;
		return this;
	}

	/**
	 * 获取用户的用户名。
	 *
	 * @return 用户的用户名
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * 设置用户的用户名。
	 *
	 * @param username 用户的用户名
	 * @return 当前对象，用于链式调用
	 */
	public Users setUsername(String username) {
		this.username = username;
		return this;
	}

	/**
	 * 获取用户的邮箱地址。
	 *
	 * @return 用户的邮箱地址
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * 设置用户的邮箱地址。
	 *
	 * @param email 用户的邮箱地址
	 * @return 当前对象，用于链式调用
	 */
	public Users setEmail(String email) {
		this.email = email;
		return this;
	}

	/**
	 * 获取用户的密码哈希值。
	 *
	 * @return 用户的密码哈希值
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * 设置用户的密码哈希值。
	 *
	 * @param passwordHash 用户的密码哈希值
	 * @return 当前对象，用于链式调用
	 */
	public Users setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
		return this;
	}

	/**
	 * 检查用户是否被封禁。
	 *
	 * @return 如果用户被封禁，返回 {@code true}，否则返回 {@code false}
	 */
	public boolean getIsBanned() {
		return isBanned;
	}

	/**
	 * 设置用户是否被封禁。
	 *
	 * @param banned 是否封禁用户
	 * @return 当前对象，用于链式调用
	 */
	public Users setIsBanned(boolean banned) {
		this.isBanned = banned;
		return this;
	}

	/**
	 * 检查用户是否是管理员。
	 *
	 * @return 如果用户是管理员，返回 {@code true}，否则返回 {@code false}
	 */
	public boolean getIsAdmin() {
		return isAdmin;
	}

	/**
	 * 设置用户是否是管理员。
	 *
	 * @param admin 是否是管理员
	 * @return 当前对象，用于链式调用
	 */
	public Users setIsAdmin(boolean admin) {
		this.isAdmin = admin;
		return this;
	}

	/**
	 * 获取用户的创建时间。
	 *
	 * @return 用户的创建时间
	 */
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * 设置用户的创建时间。
	 *
	 * @param createdAt 用户的创建时间
	 * @return 当前对象，用于链式调用
	 */
	public Users setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	/**
	 * 获取用户的最后登录时间。
	 *
	 * @return 用户的最后登录时间
	 */
	public LocalDateTime getLastLogin() {
		return lastLogin;
	}

	/**
	 * 设置用户的最后登录时间。
	 *
	 * @param lastLogin 用户的最后登录时间
	 * @return 当前对象，用于链式调用
	 */
	public Users setLastLogin(LocalDateTime lastLogin) {
		this.lastLogin = lastLogin;
		return this;
	}

	/**
	 * 返回用户对象的字符串表示。
	 *
	 * <p>该方法返回一个包含用户所有字段值的字符串，用于调试和日志记录。
	 *
	 * @return 用户对象的字符串表示
	 */
	@Override
	public String toString() {
		return (
			"Users{" +
			"userId=" +
			userId +
			", username='" +
			username +
			'\'' +
			", email='" +
			email +
			'\'' +
			", passwordHash='" +
			passwordHash +
			'\'' +
			", isBanned=" +
			isBanned +
			", isAdmin=" +
			isAdmin +
			", createdAt=" +
			createdAt +
			", lastLogin=" +
			lastLogin +
			'}'
		);
	}
}
