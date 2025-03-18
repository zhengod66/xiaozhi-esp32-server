package xiaozhi.modules.security.service.impl;

import xiaozhi.common.user.UserDetail;
import xiaozhi.modules.security.dao.SysUserTokenDao;
import xiaozhi.modules.security.entity.SysUserTokenEntity;
import xiaozhi.modules.security.service.ShiroService;
import xiaozhi.modules.sys.dao.SysUserDao;
import xiaozhi.modules.sys.entity.SysUserEntity;
import xiaozhi.modules.sys.enums.SuperAdminEnum;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class ShiroServiceImpl implements ShiroService {
    private final SysUserDao sysUserDao;
    private final SysUserTokenDao sysUserTokenDao;

    @Override
    public Set<String> getUserPermissions(UserDetail user) {
        //系统管理员，拥有最高权限
        // TODO: 暂时写死，后续改成从数据库查询
        List<String> permissionsList = new ArrayList<>();
        //用户权限列表
        Set<String> permsSet = new HashSet<>();
        for (String permissions : permissionsList) {
            if (StringUtils.isBlank(permissions)) {
                continue;
            }
            permsSet.addAll(Arrays.asList(permissions.trim().split(",")));
        }

        return permsSet;
    }

    @Override
    public SysUserTokenEntity getByToken(String token) {
        System.out.println("ShiroServiceImpl.getByToken - 查询令牌: " + token);
        SysUserTokenEntity entity = sysUserTokenDao.getByToken(token);
        if (entity == null) {
            System.out.println("数据库中没有找到对应的令牌");
            
            // 调试: 列出数据库中的所有令牌
            System.out.println("尝试列出数据库中的所有令牌:");
            try {
                List<SysUserTokenEntity> allTokens = sysUserTokenDao.selectList(null);
                if (allTokens != null && !allTokens.isEmpty()) {
                    for (SysUserTokenEntity t : allTokens) {
                        System.out.println("数据库令牌: " + t.getToken() + ", 用户ID: " + t.getUserId());
                    }
                } else {
                    System.out.println("数据库中没有任何令牌记录");
                }
            } catch (Exception e) {
                System.out.println("查询所有令牌时出错: " + e.getMessage());
            }
        } else {
            System.out.println("找到令牌, 用户ID: " + entity.getUserId() + ", 过期时间: " + entity.getExpireDate());
        }
        return entity;
    }

    @Override
    public SysUserEntity getUser(Long userId) {
        System.out.println("ShiroServiceImpl.getUser - 查询用户: " + userId);
        SysUserEntity entity = sysUserDao.selectById(userId);
        if (entity == null) {
            System.out.println("数据库中没有找到对应的用户");
        } else {
            System.out.println("找到用户, 用户名: " + entity.getUsername() + ", 状态: " + entity.getStatus());
        }
        return entity;
    }
}