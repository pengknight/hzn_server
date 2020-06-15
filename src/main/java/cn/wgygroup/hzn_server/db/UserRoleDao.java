package cn.wgygroup.hzn_server.db;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleDao extends JpaRepository<UserRoleEntity,Integer> {
    List<UserRoleEntity> findAllByUid(Integer uid);
}
