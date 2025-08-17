package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.model.dto.user.*;
import com.brotherc.documentcenter.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@Tag(name = "用户管理")
@RequestMapping("/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "创建用户")
    @PostMapping("/add")
    public Mono<ResponseDTO<Void>> add(@Valid @RequestBody UserAddDTO userAddDTO) {
        return userService.add(userAddDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据用户ID更新用户")
    @PostMapping("/updateById")
    public Mono<ResponseDTO<Void>> updateById(@Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        return userService.updateById(userUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据用户ID删除用户")
    @PostMapping("/deleteById")
    public Mono<ResponseDTO<Void>> deleteById(@Valid @RequestBody UserDeleteDTO userDeleteDTO) {
        return userService.deleteById(userDeleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "根据用户ID查询用户")
    @GetMapping("/getById")
    public Mono<ResponseDTO<UserDTO>> getById(@RequestParam Long userId) {
        return userService.getById(userId).map(ResponseDTO::success);
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Mono<ResponseDTO<Page<UserDTO>>> page(@Valid @ParameterObject UserQueryDTO userQueryDTO, @ParameterObject Pageable pageable) {
        return userService.page(userQueryDTO, pageable).map(ResponseDTO::success);
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Mono<ResponseDTO<UserTokenDTO>> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        return userService.login(userLoginDTO).map(ResponseDTO::success);
    }

}
