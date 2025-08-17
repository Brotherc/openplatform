package com.brotherc.documentcenter.service;

import cn.dev33.satoken.reactor.context.SaReactorHolder;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.brotherc.documentcenter.constants.DefaultConstant;
import com.brotherc.documentcenter.constants.UserConstant;
import com.brotherc.documentcenter.dao.UserRepository;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.user.*;
import com.brotherc.documentcenter.model.entity.User;
import com.brotherc.documentcenter.util.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    private PasswordUtil passwordUtil;

    @Transactional(rollbackFor = Exception.class)
    public Mono<User> add(UserAddDTO userAddDTO) {
        // 校验用户名是否重复（只检查未删除的用户）
        return userRepository.countByUsernameAndIsDel(userAddDTO.getUsername(), 0)
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new BusinessException(ExceptionEnum.SYS_USER_USERNAME_EXISTS));
                    }

                    User user = new User();
                    BeanUtils.copyProperties(userAddDTO, user);

                    // 加密密码
                    String encryptedPassword = passwordUtil.encryptPassword(userAddDTO.getPassword());
                    user.setPassword(encryptedPassword);

                    user.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                    user.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    user.setCreateTime(LocalDateTime.now());
                    user.setUpdateTime(LocalDateTime.now());

                    return userRepository.save(user);
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<User> updateById(UserUpdateDTO userUpdateDTO) {
        return userRepository.findById(userUpdateDTO.getUserId())
                // 只能更新未删除的用户
                .filter(user -> user.getIsDel() == 0)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_USER_UN_EXISTS)))
                .flatMap(existingUser -> {
                    // 更新用户信息
                    existingUser.setStatus(userUpdateDTO.getStatus());
                    existingUser.setNickname(userUpdateDTO.getNickname());

                    // 如果提供了密码，则更新密码（加密后存储）
                    if (StringUtils.isNotBlank(userUpdateDTO.getPassword())) {
                        String encryptedPassword = passwordUtil.encryptPassword(userUpdateDTO.getPassword());
                        existingUser.setPassword(encryptedPassword);
                    }

                    existingUser.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    existingUser.setUpdateTime(LocalDateTime.now());

                    return userRepository.save(existingUser);
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(UserDeleteDTO userDeleteDTO) {
        return userRepository.findById(userDeleteDTO.getUserId())
                // 只能删除未删除的用户
                .filter(user -> user.getIsDel() == 0)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_USER_UN_EXISTS)))
                .flatMap(user -> {
                    // 软删除
                    user.setIsDel(1);
                    user.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    user.setUpdateTime(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .then();
    }

    public Mono<UserDTO> getById(Long userId) {
        return userRepository.findById(userId)
                .filter(user -> user.getIsDel() == 0)
                .map(user -> {
                    UserDTO userDTO = new UserDTO();
                    BeanUtils.copyProperties(user, userDTO);
                    return userDTO;
                })
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_USER_UN_EXISTS)));
    }

    public Mono<Page<UserDTO>> page(UserQueryDTO userQueryDTO, Pageable pageable) {
        Criteria criteria = Criteria.where(DefaultConstant.IS_DEL).is(0);

        if (userQueryDTO.getStatus() != null) {
            criteria = criteria.and(UserConstant.STATUS).is(userQueryDTO.getStatus());
        }
        if (StringUtils.isNotBlank(userQueryDTO.getUsername())) {
            criteria = criteria.and(UserConstant.USERNAME).like("%" + userQueryDTO.getUsername() + "%");
        }
        if (StringUtils.isNotBlank(userQueryDTO.getNickname())) {
            criteria = criteria.and(UserConstant.NICKNAME).like("%" + userQueryDTO.getNickname() + "%");
        }

        Query query = Query.query(criteria)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .sort(pageable.getSort());

        return r2dbcEntityTemplate.select(User.class)
                .from(UserConstant.USER)
                .matching(query)
                .all()
                .map(user -> {
                    UserDTO userDTO = new UserDTO();
                    BeanUtils.copyProperties(user, userDTO);
                    return userDTO;
                })
                .collectList()
                .zipWith(r2dbcEntityTemplate.count(query, User.class))
                .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    public Mono<UserTokenDTO> login(UserLoginDTO userLoginDTO) {
        return userRepository.findByUsernameAndIsDel(userLoginDTO.getUsername(), 0)
                .filter(user -> {
                    // 验证密码
                    return passwordUtil.verifyPassword(userLoginDTO.getPassword(), user.getPassword());
                })
                .map(user -> {
                    UserTokenDTO userDTO = new UserTokenDTO();
                    BeanUtils.copyProperties(user, userDTO);
                    return userDTO;
                })
                .flatMap(userDTO -> SaReactorHolder.sync(() -> {
                    StpUtil.login(userDTO.getUserId());
                    SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
                    userDTO.setToken(tokenInfo.getTokenValue());
                    return userDTO;
                }))
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.LOGIN_USERNAME_PASSWORD_ERROR)));
    }

}
