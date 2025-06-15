package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.apiinfocategory.*;
import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.service.ApiInfoCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Validated
@Tag(name = "api分类")
@RequestMapping("/apiInfoCategory")
@RestController
public class ApiInfoCategoryController {

    @Autowired
    private ApiInfoCategoryService apiInfoCategoryService;

    @Operation(summary = "查询api分类树")
    @GetMapping("/getTree")
    public Mono<ResponseDTO<List<ApiInfoCategoryNodeDTO>>> getTree() {
        return apiInfoCategoryService.getTree().map(ResponseDTO::success);
    }

    @Operation(summary = "创建api分类")
    @PostMapping("/add")
    public Mono<ResponseDTO<Void>> add(@Valid @RequestBody ApiInfoCategoryAddDTO addDTO) {
        return apiInfoCategoryService.add(addDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据api分类id更新api分类")
    @PostMapping("/updateById")
    public Mono<ResponseDTO<Void>> updateById(@Valid @RequestBody ApiInfoCategoryUpdateDTO updateDTO) {
        return apiInfoCategoryService.updateById(updateDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "根据api分类id删除api分类")
    @PostMapping("/deleteById")
    public Mono<ResponseDTO<Void>> deleteById(@Valid @RequestBody ApiInfoCategoryDeleteDTO deleteDTO) {
        return apiInfoCategoryService.deleteById(deleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "根据api分类id列表删除api分类")
    @PostMapping("/deleteByIdList")
    public Mono<ResponseDTO<Void>> deleteByIdList(@Valid @RequestBody ApiInfoCategoryBatchDeleteDTO batchDeleteDTO) {
        return apiInfoCategoryService.deleteByIdList(batchDeleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "更新api分类状态")
    @PostMapping("/update/status")
    public Mono<ResponseDTO<Void>> updateStatus(@Valid @RequestBody ApiInfoCategoryStatusUpdateDTO statusUpdateDTO) {
        return apiInfoCategoryService.updateStatus(statusUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "批量更新api分类状态")
    @PostMapping("/update/status/batch")
    public Mono<ResponseDTO<Void>> updateStatusBatch(@Valid @RequestBody ApiInfoCategoryStatusBatchUpdateDTO statusUpdateDTO) {
        return apiInfoCategoryService.updateStatusBatch(statusUpdateDTO).map(o -> ResponseDTO.success());
    }

}
