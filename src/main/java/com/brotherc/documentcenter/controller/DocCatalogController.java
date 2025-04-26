package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.model.dto.doccatalog.*;
import com.brotherc.documentcenter.service.DocCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@Validated
@Tag(name = "文档目录")
@RequestMapping("/docCatalog")
@RestController
public class DocCatalogController {

    @Autowired
    private DocCatalogService docCatalogService;

    @Operation(summary = "查询文档目录树")
    @GetMapping("/getTree")
    public Mono<ResponseDTO<List<DocCatalogNodeDTO>>> getTree(@Valid @ParameterObject DocCatalogNodeQueryDTO queryDTO) {
        return docCatalogService.getTreeByGroupId(queryDTO.getDocCatalogGroupId()).map(ResponseDTO::success);
    }

    @Operation(summary = "创建文档目录")
    @PostMapping("/add")
    public Mono<ResponseDTO<Void>> add(@Valid @RequestBody DocCatalogAddDTO addDTO) {
        return docCatalogService.add(addDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据文档目录id更新文档目录")
    @PostMapping("/updateById")
    public Mono<ResponseDTO<Void>> updateById(@Valid @RequestBody DocCatalogUpdateDTO updateDTO) {
        return docCatalogService.updateById(updateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "更新文档目录状态")
    @PostMapping("/update/status")
    public Mono<ResponseDTO<Void>> updateStatus(@Valid @RequestBody DocCatalogStatusUpdateDTO statusUpdateDTO) {
        return docCatalogService.updateStatus(statusUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "批量更新文档目录状态")
    @PostMapping("/update/status/batch")
    public Mono<ResponseDTO<Void>> updateStatusBatch(@Valid @RequestBody DocCatalogStatusBatchUpdateDTO statusUpdateDTO) {
        return docCatalogService.updateStatusBatch(statusUpdateDTO).map(o -> ResponseDTO.success());
    }

    @Operation(summary = "根据文档目录id删除文档目录")
    @PostMapping("/deleteById")
    public Mono<ResponseDTO<Void>> deleteById(@Valid @RequestBody DocCatalogDeleteDTO deleteDTO) {
        return docCatalogService.deleteById(deleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "根据文档目录id列表删除分组")
    @PostMapping("/deleteByIdList")
    public Mono<ResponseDTO<Void>> deleteByIdList(@Valid @RequestBody DocCatalogBatchDeleteDTO batchDeleteDTO) {
        return docCatalogService.deleteByIdList(batchDeleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

}
