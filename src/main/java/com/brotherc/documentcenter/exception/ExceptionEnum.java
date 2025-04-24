package com.brotherc.documentcenter.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExceptionEnum {

    SYS_ERROR(1000001, "系统异常"),
    SYS_CHECK_ERROR(1000002, "系统校验异常"),
    SYS_SAVE_ERROR(1000003, "保存失败"),
    SYS_UPDATE_ERROR(1000004, "更新失败"),
    SYS_REMOVE_ERROR(1000005, "删除失败"),
    SYS_BATCH_REMOVE_ERROR(1000006, "批量删除失败"),
    SYS_PARAM_NULL_ERROR(1000007, "参数缺少"),
    SYS_SORT_REPEAT_ERROR(1000008, "排序值重复"),

    ROLE_CODE_EXISTS(1010101, "角色code已存在"),
    ROLE_UN_EXISTS(1010102, "角色不存在"),
    SYS_USER_USERNAME_EXISTS(1010103, "用户名已存在"),
    SYS_USER_UN_EXISTS(1010104, "用户不存在"),
    SYS_USER_PWD_ERROR(1010105, "密码错误"),
    MENU_UN_EXISTS(1010106, "菜单不存在"),
    MENU_UN_DELETE(1010107, "菜单不能删除"),
    LOGIN_ERROR(1010108, "登录失败"),
    LOGIN_USERNAME_NULL_ERROR(1010109, "用户名不能为空"),
    LOGIN_PASSWORD_NULL_ERROR(1010110, "密码不能为空"),
    LOGIN_USERNAME_PASSWORD_ERROR(1010111, "用户名或密码错误"),
    LOGIN_TOKEN_ERROR(1010112, "请先登录"),
    PERMISSION_ERROR(1010113, "没有权限"),
    ;

    /**
     * 应用(1~2位)、服务(2位)、模块(2位)、异常(2位)
     */
    private final Integer code;

    /**
     * 异常提示信息
     */
    private final String msg;

    public void throwException() {
        throw new BusinessException(this);
    }

    public void throwException(boolean flag) {
        if (flag) {
            throw new BusinessException(this);
        }
    }

}
