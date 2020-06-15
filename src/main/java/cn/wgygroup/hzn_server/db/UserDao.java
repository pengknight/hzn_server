package cn.wgygroup.hzn_server.db;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDao extends JpaRepository<UserEntity,Integer> {
    UserEntity findByUsername(String username);
}
