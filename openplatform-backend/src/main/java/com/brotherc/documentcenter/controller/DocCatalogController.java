package com.brotherc.documentcenter.controller;

import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoDTO;
import com.brotherc.documentcenter.model.dto.common.ResponseDTO;
import com.brotherc.documentcenter.model.dto.doccatalog.*;
import com.brotherc.documentcenter.model.dto.document.DocumentDTO;
import com.brotherc.documentcenter.model.dto.document.DocumentQueryDTO;
import com.brotherc.documentcenter.model.dto.document.DocumentSaveDTO;
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
        return docCatalogService.updateById(updateDTO).then(Mono.fromCallable(ResponseDTO::success));
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

    @Operation(summary = "根据文档目录id列表删除文档目录")
    @PostMapping("/deleteByIdList")
    public Mono<ResponseDTO<Void>> deleteByIdList(@Valid @RequestBody DocCatalogBatchDeleteDTO batchDeleteDTO) {
        return docCatalogService.deleteByIdList(batchDeleteDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "保存文章")
    @PostMapping("/saveDocument")
    public Mono<ResponseDTO<Void>> saveDocument(@Valid @RequestBody DocumentSaveDTO saveDTO) {
        return docCatalogService.saveDocument(saveDTO).then(Mono.fromCallable(ResponseDTO::success));
    }

    @Operation(summary = "根据文章id查询文章")
    @GetMapping("/getDocumentById")
    public Mono<ResponseDTO<DocumentDTO>> getDocumentById(@Valid @ParameterObject DocumentQueryDTO queryDTO) {
        return docCatalogService.getDocumentById(queryDTO).map(ResponseDTO::success);
    }

    @Operation(summary = "根据文档目录id查询api信息")
    @GetMapping("/getApiInfoById")
    public Mono<ResponseDTO<ApiInfoDTO>> getApiInfoByDocCatalogId(@Valid @ParameterObject DocCatalogApiQueryDTO queryDTO) {
        return docCatalogService.getApiByDocCatalogId(queryDTO).map(ResponseDTO::success);
    }

}
