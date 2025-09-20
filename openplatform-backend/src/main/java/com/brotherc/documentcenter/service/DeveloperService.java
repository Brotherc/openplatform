package com.brotherc.documentcenter.service;

import cn.dev33.satoken.reactor.context.SaReactorHolder;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.brotherc.documentcenter.constants.DefaultConstant;
import com.brotherc.documentcenter.constants.DeveloperConstant;
import com.brotherc.documentcenter.constants.LoginConstants;
import com.brotherc.documentcenter.dao.DeveloperRepository;
import com.brotherc.documentcenter.enums.DeveloperAuthenticateStatusEnum;
import com.brotherc.documentcenter.enums.UserStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.developer.*;
import com.brotherc.documentcenter.model.dto.user.UserLoginPortalDTO;
import com.brotherc.documentcenter.model.dto.user.UserPortalTokenDTO;
import com.brotherc.documentcenter.model.entity.Developer;
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
public class DeveloperService {

    @Autowired
    private DeveloperRepository developerRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    private PasswordUtil passwordUtil;

    @Transactional(rollbackFor = Exception.class)
    public Mono<Developer> add(DeveloperAddDTO developerAddDTO) {
        // 校验用户名是否重复（只检查未删除的开发者）
        return developerRepository.countByUsernameAndIsDel(developerAddDTO.getUsername(), 0)
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new BusinessException(ExceptionEnum.DEVELOPER_USERNAME_EXISTS));
                    }

                    Developer developer = new Developer();
                    BeanUtils.copyProperties(developerAddDTO, developer);

                    // 加密密码
                    String encryptedPassword = passwordUtil.encryptPassword(developer.getPassword());
                    developer.setPassword(encryptedPassword);

                    // 设置认证状态，默认为未认证
                    developer.setAuthenticateStatus(DeveloperAuthenticateStatusEnum.UNAUTHORIZED.getCode());

                    developer.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                    developer.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    developer.setCreateTime(LocalDateTime.now());
                    developer.setUpdateTime(LocalDateTime.now());

                    return developerRepository.save(developer);
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Developer> updateById(DeveloperUpdateDTO developerUpdateDTO) {
        return developerRepository.findById(developerUpdateDTO.getDeveloperId())
                // 只能更新未删除的开发者
                .filter(developer -> developer.getIsDel() == 0)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(existingDeveloper -> {
                    // 更新开发者信息
                    existingDeveloper.setStatus(developerUpdateDTO.getStatus());
                    existingDeveloper.setDeveloperType(developerUpdateDTO.getDeveloperType());

                    // 如果改了开发者类型，则将认证状态置为未认证，并且将开发者资质失效
                    if (developerUpdateDTO.getDeveloperType().equals(existingDeveloper.getDeveloperType())) {
                        existingDeveloper.setAuthenticateStatus(DeveloperAuthenticateStatusEnum.UNAUTHORIZED.getCode());
                        // todo
                    }

                    existingDeveloper.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    existingDeveloper.setUpdateTime(LocalDateTime.now());

                    return developerRepository.save(existingDeveloper);
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(DeveloperDeleteDTO developerDeleteDTO) {
        return developerRepository.findById(developerDeleteDTO.getDeveloperId())
                // 只能删除未删除的开发者
                .filter(developer -> developer.getIsDel() == 0)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(developer -> {
                    // 软删除
                    developer.setIsDel(1);
                    developer.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    developer.setUpdateTime(LocalDateTime.now());
                    return developerRepository.save(developer);
                })
                .then();
    }

    public Mono<DeveloperDTO> getById(Long developerId) {
        return developerRepository.findById(developerId)
                .filter(developer -> developer.getIsDel() == 0)
                .map(developer -> {
                    DeveloperDTO developerDTO = new DeveloperDTO();
                    BeanUtils.copyProperties(developer, developerDTO);
                    return developerDTO;
                })
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)));
    }

    public Mono<Page<DeveloperDTO>> page(DeveloperQueryDTO developerQueryDTO, Pageable pageable) {
        Criteria criteria = Criteria.where(DefaultConstant.IS_DEL).is(0);

        if (developerQueryDTO.getStatus() != null) {
            criteria = criteria.and(DeveloperConstant.STATUS).is(developerQueryDTO.getStatus());
        }
        if (StringUtils.isNotBlank(developerQueryDTO.getUsername())) {
            criteria = criteria.and(DeveloperConstant.USERNAME).like("%" + developerQueryDTO.getUsername() + "%");
        }
        if (developerQueryDTO.getDeveloperType() != null) {
            criteria = criteria.and(DeveloperConstant.DEVELOPER_TYPE).is(developerQueryDTO.getDeveloperType());
        }
        if (developerQueryDTO.getAuthenticateStatus() != null) {
            criteria = criteria.and(DeveloperConstant.AUTHENTICATE_STATUS).is(developerQueryDTO.getAuthenticateStatus());
        }

        Query query = Query.query(criteria)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .sort(pageable.getSort());

        return r2dbcEntityTemplate.select(Developer.class)
                .from(DeveloperConstant.DEVELOPER)
                .matching(query)
                .all()
                .map(developer -> {
                    DeveloperDTO developerDTO = new DeveloperDTO();
                    BeanUtils.copyProperties(developer, developerDTO);
                    return developerDTO;
                })
                .collectList()
                .zipWith(r2dbcEntityTemplate.count(query, Developer.class))
                .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }


    public Mono<UserPortalTokenDTO> portalLogin(UserLoginPortalDTO userLoginPortalDTO) {
        return developerRepository.findByUsernameAndIsDel(userLoginPortalDTO.getUsername(), 0)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.LOGIN_USERNAME_PASSWORD_ERROR)))
                .flatMap(developer -> {
                    // 验证密码
                    if (!passwordUtil.verifyPassword(userLoginPortalDTO.getPassword(), developer.getPassword())) {
                        return Mono.error(new BusinessException(ExceptionEnum.LOGIN_USERNAME_PASSWORD_ERROR));
                    }
                    // 判断状态
                    if (developer.getStatus() == UserStatusEnum.DISABLED.getCode()) {
                        return Mono.error(new BusinessException(ExceptionEnum.USER_DISABLED));
                    }

                    UserPortalTokenDTO userDTO = new UserPortalTokenDTO();
                    BeanUtils.copyProperties(developer, userDTO);

                    return SaReactorHolder.sync(() -> {
                        StpUtil.login(LoginConstants.DEVELOPER_PREFIX + userDTO.getDeveloperId());
                        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
                        userDTO.setToken(tokenInfo.getTokenValue());
                        return userDTO;
                    });
                });
    }

}
