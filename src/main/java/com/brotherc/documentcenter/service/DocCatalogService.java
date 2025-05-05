package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.dao.DocCatalogRepository;
import com.brotherc.documentcenter.enums.DocCatalogTypeEnum;
import com.brotherc.documentcenter.enums.PublishStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.doccatalog.*;
import com.brotherc.documentcenter.model.dto.document.DocumentDTO;
import com.brotherc.documentcenter.model.dto.document.DocumentQueryDTO;
import com.brotherc.documentcenter.model.dto.document.DocumentSaveDTO;
import com.brotherc.documentcenter.model.entity.DocCatalog;
import com.brotherc.documentcenter.model.pojo.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DocCatalogService {

    @Autowired
    private DocCatalogRepository docCatalogRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    private TransactionalOperator transactionalOperator;
    @Autowired
    private DocumentService documentService;

    public Mono<List<DocCatalogNodeDTO>> getTreeByGroupId(Long docCatalogGroupId) {
        return docCatalogRepository.findByDocCatalogGroupId(docCatalogGroupId)
                .collectList()
                .map(o -> buildCatalogTree(o, 0L));
    }

    private List<DocCatalogNodeDTO> buildCatalogTree(List<DocCatalog> all, Long parentId) {
        List<DocCatalogNodeDTO> result = new ArrayList<>();

        // 排序
        all.sort(Comparator.comparing(DocCatalog::getSort, Comparator.nullsLast(Integer::compareTo)));

        List<DocCatalogNodeDTO> copyList = all.stream().map(o -> {
            DocCatalogNodeDTO docCatalogNodeDTO = new DocCatalogNodeDTO();
            BeanUtils.copyProperties(o, docCatalogNodeDTO);
            return docCatalogNodeDTO;
        }).toList();

        copyList.forEach(item -> {
            if (parentId.equals(item.getParentId())) {
                item.setChildren(buildCatalogTree(all, item.getDocCatalogId()));
                result.add(item);
            }
        });
        return result;
    }

    public Mono<DocCatalog> add(DocCatalogAddDTO addDTO) {
        return transactionalOperator.execute(status ->
                        docCatalogRepository.countByDocCatalogGroupIdAndParentIdAndSort(
                                addDTO.getDocCatalogGroupId(), addDTO.getParentId(), addDTO.getSort().intValue()
                        ).flatMap(count -> {
                            if (count > 0) {
                                return Mono.error(new BusinessException(ExceptionEnum.SYS_SORT_REPEAT_ERROR));
                            }

                            DocCatalog docCatalog = new DocCatalog();
                            BeanUtils.copyProperties(addDTO, docCatalog);
                            docCatalog.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());
                            docCatalog.setSort(addDTO.getSort().intValue());
                            docCatalog.setCreateBy(1L);
                            docCatalog.setUpdateBy(1L);
                            docCatalog.setCreateTime(LocalDateTime.now());
                            docCatalog.setUpdateTime(LocalDateTime.now());

                            return docCatalogRepository.save(docCatalog);
                        })
                )
                .flatMap(docCatalog -> {
                    // 事务提交后将文章写到es
                    if (DocCatalogTypeEnum.DOC.getCode() == docCatalog.getType()) {
                        return documentService.add(docCatalog, StringUtils.EMPTY)
                                .onErrorResume(e -> {
                                    log.error("插入ES失败（文章ID: {}），错误信息：{}", docCatalog.getDocCatalogId(), e.getMessage(), e);
                                    return Mono.empty();
                                })
                                .thenReturn(docCatalog);
                    } else {
                        return Mono.just(docCatalog);
                    }
                })
                .single();
    }

    public Mono<Void> updateById(DocCatalogUpdateDTO updateDTO) {
        return transactionalOperator.execute(status ->
                docCatalogRepository.findById(updateDTO.getDocCatalogId())
                        .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                        .flatMap(original -> {
                            boolean sortChange = !Objects.equals(original.getParentId(), updateDTO.getParentId()) ||
                                    !Objects.equals(updateDTO.getSort().intValue(), original.getSort());
                            Mono<Boolean> sortRepeat = sortChange ? docCatalogRepository.countByDocCatalogGroupIdAndParentIdAndSort(
                                    original.getDocCatalogGroupId(), original.getParentId(), updateDTO.getSort().intValue()
                            ).map(count -> count > 0) : Mono.just(false);

                            return sortRepeat.flatMap(repeat -> {
                                if (repeat) {
                                    return Mono.error(new BusinessException(ExceptionEnum.SYS_SORT_REPEAT_ERROR));
                                }

                                original.setName(updateDTO.getName());
                                original.setParentId(updateDTO.getParentId());
                                original.setSort(updateDTO.getSort().intValue());
                                original.setUpdateBy(1L);
                                original.setUpdateTime(LocalDateTime.now());

                                return docCatalogRepository.save(original);
                            });
                        })
        ).flatMap(docCatalog -> {
            if (DocCatalogTypeEnum.DOC.getCode() == docCatalog.getType()) {
                // 查询 ES 是否已有该文档
                return documentService.getById(updateDTO.getDocCatalogId())
                        .switchIfEmpty(
                                // 如果不存在，则新增文档
                                documentService.add(docCatalog, StringUtils.EMPTY).then(Mono.empty())
                        )
                        .flatMap(document -> {
                            // 如果存在，更新文档
                            return documentService.update(updateDTO).then();
                        });
            } else {
                return Mono.empty();
            }
        }).single();
    }


    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatus(DocCatalogStatusUpdateDTO statusUpdateDTO) {
        Query query = Query.query(Criteria.where("doc_catalog_id").is(statusUpdateDTO.getDocCatalogId()));
        Update update = Update
                .update("status", statusUpdateDTO.getStatus())
                .set("updateTime", LocalDateTime.now())
                .set("updateBy", 1L);

        return r2dbcEntityTemplate.update(query, update, DocCatalog.class);
        // TODO 如果是文章，并且是发布则往es写发布文章，否则删除发布文章
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatusBatch(DocCatalogStatusBatchUpdateDTO statusUpdateDTO) {
        Query query = Query.query(Criteria.where("doc_catalog_id").in(statusUpdateDTO.getDocCatalogIdList()));
        Update update = Update
                .update("status", statusUpdateDTO.getStatus())
                .set("updateTime", LocalDateTime.now())
                .set("updateBy", 1L);

        return r2dbcEntityTemplate.update(query, update, DocCatalog.class);
        // TODO 如果是文章，并且是发布则往es写发布文章，否则删除发布文章
    }

    public Mono<Void> deleteById(DocCatalogDeleteDTO deleteDTO) {
        return transactionalOperator.execute(status ->
                docCatalogRepository.findById(deleteDTO.getDocCatalogId())
                        .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                        .flatMap(catalog ->
                                docCatalogRepository.countByParentId(deleteDTO.getDocCatalogId())
                                        .flatMap(count -> {
                                            if (count > 0) {
                                                return Mono.error(new BusinessException(ExceptionEnum.CATALOG_EXIST_CHILDREN_ERROR));
                                            }

                                            return docCatalogRepository.deleteById(deleteDTO.getDocCatalogId()).thenReturn(catalog);
                                        })
                        )
        ).flatMap(docCatalog -> {
            if (DocCatalogTypeEnum.DOC.getCode() == docCatalog.getType()) {
                // 删除 ES 文档
                return documentService.deleteById(docCatalog.getDocCatalogId());
            } else {
                return Mono.empty();
            }
        }).then();
    }

    public Mono<Void> deleteByIdList(DocCatalogBatchDeleteDTO batchDeleteDTO) {
        Query query = Query.query(Criteria.where("doc_catalog_id").in(batchDeleteDTO.getDocCatalogIdList()));
        return transactionalOperator.execute(status ->
                r2dbcEntityTemplate
                        .delete(query, DocCatalog.class)
        ).flatMap(result ->
                documentService.deleteByIdList(batchDeleteDTO.getDocCatalogIdList())
        ).then();
    }

    public Mono<Void> saveDocument(DocumentSaveDTO saveDTO) {
        return docCatalogRepository.findById(saveDTO.getDocCatalogId())
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(docCatalog -> {
                    if (DocCatalogTypeEnum.DOC.getCode() == docCatalog.getType()) {
                        // 查询 ES 是否已有该文档
                        return documentService.getById(saveDTO.getDocCatalogId())
                                .switchIfEmpty(
                                        // 如果不存在，则新增文档
                                        documentService.add(docCatalog, saveDTO.getContent()).then(Mono.empty())
                                )
                                .flatMap(document -> {
                                    // 如果存在，更新文档
                                    return documentService.updateContentById(saveDTO.getDocCatalogId(), saveDTO.getContent()).then();
                                });
                    } else {
                        return Mono.empty();
                    }
                });
    }

    public Mono<DocumentDTO> getDocumentById(DocumentQueryDTO queryDTO) {
        return documentService.getById(queryDTO.getId()).switchIfEmpty(
                Mono.just(new Document())
        ).map(document -> {
            DocumentDTO documentDTO = new DocumentDTO();
            BeanUtils.copyProperties(document, documentDTO);
            return documentDTO;
        });
    }

}
