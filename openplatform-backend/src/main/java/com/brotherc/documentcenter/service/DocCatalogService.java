package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.constants.DefaultConstant;
import com.brotherc.documentcenter.constants.DocCatalogConstant;
import com.brotherc.documentcenter.dao.DocCatalogRepository;
import com.brotherc.documentcenter.dao.DocCatalogApiRepository;
import com.brotherc.documentcenter.enums.DocCatalogTypeEnum;
import com.brotherc.documentcenter.enums.PublishStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoDTO;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoQueryDTO;
import com.brotherc.documentcenter.model.dto.doccatalog.*;
import com.brotherc.documentcenter.model.dto.document.DocumentDTO;
import com.brotherc.documentcenter.model.dto.document.DocumentQueryDTO;
import com.brotherc.documentcenter.model.dto.document.DocumentSaveDTO;
import com.brotherc.documentcenter.model.entity.DocCatalog;
import com.brotherc.documentcenter.model.entity.DocCatalogApi;
import com.brotherc.documentcenter.model.pojo.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class DocCatalogService {

    @Autowired
    private DocCatalogRepository docCatalogRepository;
    @Autowired
    private DocCatalogApiRepository docCatalogApiRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    private TransactionalOperator transactionalOperator;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private ApiInfoCategoryService apiInfoCategoryService;

    public Mono<List<DocCatalogNodeDTO>> getTreeByGroupId(Long docCatalogGroupId) {
        return docCatalogRepository.findByDocCatalogGroupId(docCatalogGroupId)
                .collectList()
                .map(o -> buildCatalogTree(o, 0L));
    }

    public Mono<List<DocCatalogNodeDTO>> getTreePortalByGroupId(Long docCatalogGroupId, Integer status) {
        return docCatalogRepository.findByDocCatalogGroupIdAndStatus(docCatalogGroupId, status)
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

        // 设置子节点
        for (DocCatalogNodeDTO item : copyList) {
            if (parentId.equals(item.getParentId())) {
                item.setChildren(buildCatalogTree(all, item.getDocCatalogId()));
                result.add(item);
            }
        }

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
                            docCatalog.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                            docCatalog.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                            docCatalog.setCreateTime(LocalDateTime.now());
                            docCatalog.setUpdateTime(LocalDateTime.now());

                            return docCatalogRepository.save(docCatalog)
                                    .flatMap(savedDocCatalog -> {
                                        // 如果类型是API，保存文档目录与API的关系
                                        if (DocCatalogTypeEnum.API.getCode() == savedDocCatalog.getType()) {
                                            DocCatalogApi docCatalogApi = new DocCatalogApi();
                                            docCatalogApi.setDocCatalogId(savedDocCatalog.getDocCatalogId());
                                            docCatalogApi.setDocCatalogGroupId(savedDocCatalog.getDocCatalogGroupId());
                                            docCatalogApi.setParentId(savedDocCatalog.getParentId());
                                            docCatalogApi.setSort(savedDocCatalog.getSort());
                                            docCatalogApi.setApiInfoCategoryId(addDTO.getApiInfoCategoryId());
                                            docCatalogApi.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                                            docCatalogApi.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                            docCatalogApi.setCreateTime(LocalDateTime.now());
                                            docCatalogApi.setUpdateTime(LocalDateTime.now());

                                            return docCatalogApiRepository.save(docCatalogApi).thenReturn(savedDocCatalog);
                                        } else {
                                            return Mono.just(savedDocCatalog);
                                        }
                                    });
                        })
                )
                .flatMap(docCatalog -> {
                    // 事务提交后将文章写到es
                    if (DocCatalogTypeEnum.DOC.getCode() == docCatalog.getType()) {
                        return documentService.add(docCatalog, StringUtils.EMPTY)
                                .onErrorResume(e -> {
                                    log.error("插入ES失败（文章ID: {}）", docCatalog.getDocCatalogId(), e);
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
                            // 判断排序值是否发生变化
                            boolean sortChange = !Objects.equals(original.getParentId(), updateDTO.getParentId()) ||
                                    !Objects.equals(updateDTO.getSort().intValue(), original.getSort());
                            // 如果排序值变化，校验排序值是否重复
                            Mono<Boolean> sortRepeat = sortChange ? docCatalogRepository.countByDocCatalogGroupIdAndParentIdAndSort(
                                    original.getDocCatalogGroupId(), updateDTO.getParentId(), updateDTO.getSort().intValue()
                            ).map(count -> count > 0) : Mono.just(false);

                            return sortRepeat.flatMap(repeat -> {
                                if (Objects.equals(true, repeat)) {
                                    return Mono.error(new BusinessException(ExceptionEnum.SYS_SORT_REPEAT_ERROR));
                                }

                                original.setName(updateDTO.getName());
                                original.setParentId(updateDTO.getParentId());
                                original.setSort(updateDTO.getSort().intValue());
                                original.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                original.setUpdateTime(LocalDateTime.now());

                                return docCatalogRepository.save(original)
                                        .flatMap(saved -> {
                                            // 如果类型是API
                                            if (DocCatalogTypeEnum.API.getCode() == saved.getType()) {
                                                // 判断 文档目录api 是否存在
                                                return docCatalogApiRepository.findByDocCatalogId(saved.getDocCatalogId())
                                                        .flatMap(existing -> {
                                                            // 存在则更新
                                                            existing.setApiInfoCategoryId(updateDTO.getApiInfoCategoryId());
                                                            existing.setParentId(updateDTO.getParentId());
                                                            existing.setSort(updateDTO.getSort().intValue());
                                                            existing.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                                            existing.setUpdateTime(LocalDateTime.now());
                                                            return docCatalogApiRepository.save(existing);
                                                        })
                                                        .switchIfEmpty(Mono.defer(() -> {
                                                            // 不存在则创建
                                                            DocCatalogApi newApi = new DocCatalogApi();
                                                            newApi.setDocCatalogId(saved.getDocCatalogId());
                                                            newApi.setDocCatalogGroupId(saved.getDocCatalogGroupId());
                                                            newApi.setParentId(saved.getParentId());
                                                            newApi.setSort(saved.getSort());
                                                            newApi.setApiInfoCategoryId(updateDTO.getApiInfoCategoryId());
                                                            newApi.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                                                            newApi.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                                            newApi.setCreateTime(LocalDateTime.now());
                                                            newApi.setUpdateTime(LocalDateTime.now());
                                                            newApi.setIsDel(0);
                                                            return docCatalogApiRepository.save(newApi);
                                                        }))
                                                        .thenReturn(saved);
                                            } else {
                                                return Mono.just(saved);
                                            }
                                        });
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
                        .flatMap(document ->
                                // 如果存在，更新文档
                                documentService.updateById(updateDTO).then()
                        )
                        .onErrorResume(e -> {
                            log.error("更新ES失败（文章ID: {}）", docCatalog.getDocCatalogId(), e);
                            return Mono.empty();
                        });
            } else {
                return Mono.empty();
            }
        }).then();
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatus(DocCatalogStatusUpdateDTO statusUpdateDTO) {
        Query query = Query.query(Criteria.where(DocCatalogConstant.DOC_CATALOG_ID).is(statusUpdateDTO.getDocCatalogId()));
        Update update = Update
                .update(DocCatalogConstant.STATUS, statusUpdateDTO.getStatus())
                .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

        return r2dbcEntityTemplate.update(query, update, DocCatalog.class);
        // TODO 如果是文章，并且是发布则往es写发布文章，否则删除发布文章
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatusBatch(DocCatalogStatusBatchUpdateDTO statusUpdateDTO) {
        Query query = Query.query(Criteria.where(DocCatalogConstant.DOC_CATALOG_ID).in(statusUpdateDTO.getDocCatalogIdList()));
        Update update = Update
                .update(DocCatalogConstant.STATUS, statusUpdateDTO.getStatus())
                .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

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
                                                return Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_EXIST_CHILDREN_ERROR));
                                            }

                                            Mono<Void> deleteCatalogApi = Mono.empty();
                                            if (DocCatalogTypeEnum.API.getCode() == catalog.getType()) {
                                                // 删除文档目录api
                                                deleteCatalogApi = docCatalogApiRepository.deleteByDocCatalogId(catalog.getDocCatalogId());
                                            }

                                            return deleteCatalogApi
                                                    .then(docCatalogRepository.deleteById(deleteDTO.getDocCatalogId()))
                                                    .thenReturn(catalog);
                                        })
                        )
        ).flatMap(docCatalog -> {
            if (DocCatalogTypeEnum.DOC.getCode() == docCatalog.getType()) {
                // 删除 ES 文档
                return documentService.deleteById(docCatalog.getDocCatalogId())
                        .onErrorResume(e -> {
                            log.error("删除ES失败（文章ID: {}）", docCatalog.getDocCatalogId(), e);
                            return Mono.empty();
                        });
            } else {
                return Mono.empty();
            }
        }).then();
    }

    public Mono<Void> deleteByIdList(DocCatalogBatchDeleteDTO batchDeleteDTO) {
        List<Long> idList = batchDeleteDTO.getDocCatalogIdList();
        Query query = Query.query(Criteria.where(DocCatalogConstant.DOC_CATALOG_ID).in(idList));

        return transactionalOperator.execute(status -> {
            Mono<Long> deleteCatalogMono = r2dbcEntityTemplate.delete(query, DocCatalog.class);
            Mono<Long> deleteCatalogApiMono = r2dbcEntityTemplate.delete(query, DocCatalogApi.class);

            return deleteCatalogApiMono.then(deleteCatalogMono);
        }).flatMap(result ->
                documentService.deleteByIdList(idList)
                        .onErrorResume(e -> {
                            log.error("删除ES失败（文章ID: {}）", idList, e);
                            return Mono.empty();
                        })
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
                                .flatMap(document ->
                                        // 如果存在，更新文档
                                        documentService.updateContentById(saveDTO.getDocCatalogId(), saveDTO.getContent()).then()
                                );
                    } else {
                        return Mono.empty();
                    }
                });
    }

    public Mono<DocumentDTO> getDocumentById(DocumentQueryDTO queryDTO) {
        return documentService.getById(queryDTO.getId())
                .map(document -> {
                    DocumentDTO dto = new DocumentDTO();
                    BeanUtils.copyProperties(document, dto);
                    return dto;
                })
                .switchIfEmpty(Mono.fromCallable(() -> null));
    }

    public Mono<ApiInfoDTO> getApiByDocCatalogId(DocCatalogApiQueryDTO queryDTO) {
        return docCatalogApiRepository.findByDocCatalogId(queryDTO.getDocCatalogId())
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(docCatalogApi -> {
                    ApiInfoQueryDTO apiInfoQueryDTO = new ApiInfoQueryDTO();
                    apiInfoQueryDTO.setApiInfoCategoryId(docCatalogApi.getApiInfoCategoryId());
                    return apiInfoCategoryService.getByApiInfoCategoryId(apiInfoQueryDTO);
                });
    }

}
