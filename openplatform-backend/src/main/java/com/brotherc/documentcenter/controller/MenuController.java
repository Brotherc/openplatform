package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.model.dto.menu.*;
import com.brotherc.documentcenter.service.MenuService;
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

import java.util.List;

@Validated
@Tag(name = "菜单")
@RequestMapping("/menu")
@RestController
public class MenuController {

    @Autowired
    private MenuService menuService;

    @Operation(summary = "创建菜单")
    @PostMapping("/add")
    public Mono<ResponseDTO<Void>> add(@Valid @RequestBody MenuAddDTO menuAddDTO) {
        return menuService.add(menuAddDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "分页查询菜单")
    @GetMapping("/page")
    public Mono<ResponseDTO<Page<MenuDTO>>> page(@ParameterObject MenuQueryDTO menuQueryDTO, @ParameterObject Pageable pageable) {
        return menuService.page(menuQueryDTO, pageable).map(ResponseDTO::success);
    }

    @Operation(summary = "根据菜单id更新菜单")
    @PostMapping("/updateById")
    public Mono<ResponseDTO<Void>> updateById(@Valid @RequestBody MenuUpdateDTO menuUpdateDTO) {
        return menuService.updateById(menuUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据菜单id删除菜单")
    @PostMapping("/deleteById")
    public Mono<ResponseDTO<Void>> deleteById(@Valid @RequestBody MenuDeleteDTO deleteDTO) {
        return menuService.deleteById(deleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "查询菜单树")
    @GetMapping("/tree")
    public Mono<ResponseDTO<List<MenuDTO>>> getTree() {
        return menuService.getTree().map(ResponseDTO::success);
    }

}
