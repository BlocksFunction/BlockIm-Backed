package aur.im.backed.Repository;

import aur.im.backed.Entry.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
	/**
	 * 检查指定的用户名是否存在。
	 *
	 * <p>该方法通过查询数据库，判断是否存在与指定用户名匹配的用户记录。
	 * 如果存在，返回 {@code true}，否则返回 {@code false}。
	 *
	 * @param username 要检查的用户名
	 * @return 如果用户名存在，返回 {@code true}，否则返回 {@code false}
	 */
	@Query("SELECT COUNT(u) > 0 FROM Users u WHERE u.username = :username")
	boolean existsByUsername(String username);

	/**
	 * 根据用户名查找用户实体。
	 *
	 * <p>该方法通过用户名查询数据库，返回与用户名匹配的用户实体。
	 * 如果没有找到匹配的用户，返回 {@code null}。
	 *
	 * @param username 用户名
	 * @return 匹配的用户实体，如果不存在则返回 {@code null}
	 */
	Users findByUsername(String username);

	Users findByUserId(Long userId);

	Users findByEmail(String email);
}
