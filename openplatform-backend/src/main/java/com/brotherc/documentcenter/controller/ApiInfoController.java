package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoDTO;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoQueryDTO;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoSaveDTO;
import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.service.ApiInfoCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Validated
@Tag(name = "api信息")
@RequestMapping("/apiInfo")
@RestController
public class ApiInfoController {

    @Autowired
    private ApiInfoCategoryService apiInfoCategoryService;

    @Operation(summary = "保存api信息")
    @PostMapping("/save")
    public Mono<ResponseDTO<Void>> save(@Valid @RequestBody ApiInfoSaveDTO addDTO) {
        return apiInfoCategoryService.save(addDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据api分类id查询api信息")
    @GetMapping("/getByApiInfoCategoryId")
    public Mono<ResponseDTO<ApiInfoDTO>> getByApiInfoCategoryId(@Valid @ParameterObject ApiInfoQueryDTO queryDTO) {
        return apiInfoCategoryService.getByApiInfoCategoryId(queryDTO).map(ResponseDTO::success);
    }

}
