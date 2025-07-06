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
@Table(name = "api_info")
public class ApiInfo extends BaseEntity {

    /**
     * api信息id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long apiInfoId;

    /**
     * 接口名称(英文)
     */
    private String name;

    /**
     * 接口名称(中文)
     */
    private String cnName;

    /**
     * 描述
     */
    private String description;

    /**
     * 唯一标识
     */
    private String code;

    /**
     * 访问地址
     */
    private String host;

    /**
     * 请求方式
     */
    private String reqMethod;

    /**
     * 访问地址前缀
     */
    private String reqContextPath;

    /**
     * 访问地址
     */
    private String reqPath;

    /**
     * 入参信息json，请求头、请求体、请求参数等
     */
    private String reqParam;

    /**
     * 入参信息json，请求头、请求体、请求参数等，用于展示
     */
    private String reqParamDisplay;

    /**
     * 返参信息json
     */
    private String returnInfo;

    /**
     * 返参信息json，用于展示
     */
    private String returnInfoDisplay;

    /**
     * api分类id
     */
    private Long apiInfoCategoryId;

    /**
     * 状态，1：未发布，2：已发布
     */
    private Integer status;

}
