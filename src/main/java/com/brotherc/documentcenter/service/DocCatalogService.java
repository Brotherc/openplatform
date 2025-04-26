package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.dao.DocCatalogRepository;
import com.brotherc.documentcenter.enums.PublishStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.doccatalog.*;
import com.brotherc.documentcenter.model.entity.DocCatalog;
import com.brotherc.documentcenter.model.entity.DocCatalogGroup;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DocCatalogService {

    @Autowired
    private DocCatalogRepository docCatalogRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

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

    @Transactional(rollbackFor = Exception.class)
    public Mono<DocCatalog> add(DocCatalogAddDTO addDTO) {
        return docCatalogRepository.countByDocCatalogGroupIdAndParentIdAndSort(
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
            // TODO 如果是文章，往es添加文章
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<DocCatalog> updateById(DocCatalogUpdateDTO updateDTO) {
        return docCatalogRepository.findById(updateDTO.getDocCatalogId())
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
                            // TODO 如果是文章，往es更新文章
                        });
                });
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

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(DocCatalogDeleteDTO deleteDTO) {
        return docCatalogRepository.findById(deleteDTO.getDocCatalogId())
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(catalog ->
                        docCatalogRepository.countByParentId(deleteDTO.getDocCatalogId())
                                .flatMap(count -> {
                                    if (count > 0) {
                                        return Mono.error(new BusinessException(ExceptionEnum.CATALOG_EXIST_CHILDREN_ERROR));
                                    }

                                    return docCatalogRepository.deleteById(deleteDTO.getDocCatalogId());
                                })
                );
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteByIdList(DocCatalogBatchDeleteDTO batchDeleteDTO) {
        Query query = Query.query(Criteria.where("doc_catalog_id").in(batchDeleteDTO.getDocCatalogIdList()));
        return r2dbcEntityTemplate
                .delete(query, DocCatalog.class)
                .then();
    }

}
