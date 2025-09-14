package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.model.dto.developer.*;
import com.brotherc.documentcenter.service.DeveloperService;
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
@Tag(name = "开发者管理")
@RequestMapping("/developer")
@RestController
public class DeveloperController {

    @Autowired
    private DeveloperService developerService;

    @Operation(summary = "创建开发者")
    @PostMapping("/add")
    public Mono<ResponseDTO<Void>> add(@Valid @RequestBody DeveloperAddDTO developerAddDTO) {
        return developerService.add(developerAddDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据用户ID更新开发者")
    @PostMapping("/updateById")
    public Mono<ResponseDTO<Void>> updateById(@Valid @RequestBody DeveloperUpdateDTO developerUpdateDTO) {
        return developerService.updateById(developerUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据用户ID删除开发者")
    @PostMapping("/deleteById")
    public Mono<ResponseDTO<Void>> deleteById(@Valid @RequestBody DeveloperDeleteDTO developerDeleteDTO) {
        return developerService.deleteById(developerDeleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "根据用户ID查询开发者")
    @GetMapping("/getById")
    public Mono<ResponseDTO<DeveloperDTO>> getById(@RequestParam Long developerId) {
        return developerService.getById(developerId).map(ResponseDTO::success);
    }

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Mono<ResponseDTO<Page<DeveloperDTO>>> page(@Valid @ParameterObject DeveloperQueryDTO developerQueryDTO, @ParameterObject Pageable pageable) {
        return developerService.page(developerQueryDTO, pageable).map(ResponseDTO::success);
    }

}
