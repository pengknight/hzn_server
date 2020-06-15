package cn.wgygroup.hzn_server.services;

import cn.wgygroup.hzn_server.db.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    @Resource
    UserDao userDao;

    @Resource
    RoleDao roleDao;

    @Resource
    UserRoleDao userRoleDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userDao.findByUsername(username);
        if (user==null){
            throw new UsernameNotFoundException("账户不存在！");
        }
        List<UserRoleEntity> roles = userRoleDao.findAllByUid(user.getId());
        List<RoleEntity> roleEntities=new ArrayList<>();
        for (UserRoleEntity role : roles) {
            int rid = role.getRid();
            RoleEntity one = roleDao.getOne(rid);
            roleEntities.add(one);
        }
        user.setRoles(roleEntities);
        return user;
    }
}
