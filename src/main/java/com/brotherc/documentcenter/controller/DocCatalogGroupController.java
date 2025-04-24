package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.model.dto.doccataloggroup.*;
import com.brotherc.documentcenter.service.DocCatalogGroupService;
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
@Tag(name = "文档分组")
@RequestMapping("/docCatalogGroup")
@RestController
public class DocCatalogGroupController {

    @Autowired
    private DocCatalogGroupService docCatalogGroupService;

    @Operation(summary = "根据分组id查询分组")
    @GetMapping("/getById")
    public Mono<ResponseDTO<DocCatalogGroupDTO>> getById(@Valid @ParameterObject DocCatalogGroupDetailQueryDTO docCatalogGroupDetailQueryDTO) {
        return docCatalogGroupService.getById(docCatalogGroupDetailQueryDTO).map(ResponseDTO::success);
    }

    @Operation(summary = "查询分组列表")
    @GetMapping("/getList")
    public Mono<ResponseDTO<List<DocCatalogGroupDTO>>> getList() {
        return docCatalogGroupService.getList().collectList().map(ResponseDTO::success);
    }

    @Operation(summary = "分页查询分组")
    @GetMapping("/page")
    public Mono<ResponseDTO<Page<DocCatalogGroupDTO>>> page(
            @ParameterObject DocCatalogGroupQueryDTO docCatalogGroupQueryDTO, @ParameterObject Pageable pageable
    ) {
        return docCatalogGroupService.page(docCatalogGroupQueryDTO, pageable).map(ResponseDTO::success);
    }

    @Operation(summary = "创建分组")
    @PostMapping("/add")
    public Mono<ResponseDTO<Void>> add(@Valid @RequestBody DocCatalogGroupAddDTO docCatalogGroupAddDTO) {
        return docCatalogGroupService.add(docCatalogGroupAddDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据分组id更新分组")
    @PostMapping("/updateById")
    public Mono<ResponseDTO<Void>> updateById(@Valid @RequestBody DocCatalogGroupUpdateDTO docCatalogGroupUpdateDTO) {
        return docCatalogGroupService.updateById(docCatalogGroupUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "更新分组状态")
    @PostMapping("/update/status")
    public Mono<ResponseDTO<Void>> updateStatus(@Valid @RequestBody DocCatalogGroupStatusUpdateDTO docCatalogGroupStatusUpdateDTO) {
        return docCatalogGroupService.updateStatus(docCatalogGroupStatusUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据分组id删除分组")
    @PostMapping("/deleteById")
    public Mono<ResponseDTO<Void>> deleteById(@Valid @RequestBody DocCatalogGroupDeleteDTO docCatalogGroupDeleteDTO) {
        return docCatalogGroupService.deleteById(docCatalogGroupDeleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

}
