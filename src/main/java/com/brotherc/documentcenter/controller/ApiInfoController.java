package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoSaveDTO;
import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.service.ApiInfoCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
