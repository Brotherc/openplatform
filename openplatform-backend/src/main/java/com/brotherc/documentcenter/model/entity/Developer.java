package com.brotherc.documentcenter.model.entity;

import com.brotherc.documentcenter.model.entity.common.BaseEntity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Table(name = "developer")
public class Developer extends BaseEntity {

    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long developerId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 开发者类型，1：个人，2：企业
     */
    private Integer developerType;

    /**
     * 状态，1：禁用，2：启用
     */
    private Integer status;

    /**
     * 认证状态，1：已认证，2：未认证，3：审核中，4：认证未通过
     */
    private Integer authenticateStatus;

}
