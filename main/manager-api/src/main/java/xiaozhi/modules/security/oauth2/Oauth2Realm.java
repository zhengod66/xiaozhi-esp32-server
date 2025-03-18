package xiaozhi.modules.security.oauth2;

import xiaozhi.common.exception.ErrorCode;
import xiaozhi.common.user.UserDetail;
import xiaozhi.common.utils.ConvertUtils;
import xiaozhi.common.utils.MessageUtils;
import xiaozhi.modules.security.entity.SysUserTokenEntity;
import xiaozhi.modules.security.service.ShiroService;
import xiaozhi.modules.sys.entity.SysUserEntity;
import jakarta.annotation.Resource;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * 认证
 * Copyright (c) 人人开源 All rights reserved.
 * Website: https://www.renren.io
 */
@Component
public class Oauth2Realm extends AuthorizingRealm {
    @Lazy
    @Resource
    private ShiroService shiroService;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof Oauth2Token;
    }

    /**
     * 授权(验证权限时调用)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        UserDetail user = (UserDetail) principals.getPrimaryPrincipal();

        //用户权限列表
        Set<String> permsSet = shiroService.getUserPermissions(user);

        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.setStringPermissions(permsSet);
        return info;
    }

    /**
     * 认证(登录时调用)
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String accessToken = (String) token.getPrincipal();
        
        System.out.println("=====================================");
        System.out.println("Oauth2Realm.doGetAuthenticationInfo - 开始验证令牌: " + accessToken);

        //根据accessToken，查询用户信息
        SysUserTokenEntity tokenEntity = shiroService.getByToken(accessToken);
        
        if (tokenEntity == null) {
            System.out.println("令牌验证失败: 令牌不存在于数据库中");
            throw new IncorrectCredentialsException(MessageUtils.getMessage(ErrorCode.TOKEN_INVALID));
        }
        
        System.out.println("数据库中找到令牌信息: userId=" + tokenEntity.getUserId() + ", 过期时间=" + tokenEntity.getExpireDate());
        
        //token失效
        if (tokenEntity.getExpireDate().getTime() < System.currentTimeMillis()) {
            System.out.println("令牌验证失败: 令牌已过期，当前时间: " + System.currentTimeMillis() + ", 过期时间: " + tokenEntity.getExpireDate().getTime());
            throw new IncorrectCredentialsException(MessageUtils.getMessage(ErrorCode.TOKEN_INVALID));
        }

        //查询用户信息
        SysUserEntity userEntity = shiroService.getUser(tokenEntity.getUserId());
        
        if (userEntity == null) {
            System.out.println("令牌验证失败: 找不到用户信息，userId: " + tokenEntity.getUserId());
            throw new UnknownAccountException(MessageUtils.getMessage(ErrorCode.UNAUTHORIZED));
        }
        
        System.out.println("找到用户信息: " + userEntity.getUsername() + ", 状态: " + userEntity.getStatus());

        //转换成UserDetail对象
        UserDetail userDetail = ConvertUtils.sourceToTarget(userEntity, UserDetail.class);

        //获取用户对应的部门数据权限
        userDetail.setDeptIdList(null);
        userDetail.setToken(accessToken);

        //账号锁定
        if (userDetail.getStatus() == 0) {
            System.out.println("令牌验证失败: 账号已锁定");
            throw new LockedAccountException(MessageUtils.getMessage(ErrorCode.ACCOUNT_LOCK));
        }

        System.out.println("令牌验证成功!");
        System.out.println("=====================================");
        
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(userDetail, accessToken, getName());
        return info;
    }

}