package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.constants.DefaultConstant;
import com.brotherc.documentcenter.constants.DocCatalogGroupConstant;
import com.brotherc.documentcenter.dao.DocCatalogGroupRepository;
import com.brotherc.documentcenter.dao.DocCatalogRepository;
import com.brotherc.documentcenter.enums.PublishStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.doccataloggroup.*;
import com.brotherc.documentcenter.model.entity.DocCatalogGroup;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;

@Service
public class DocCatalogGroupService {

    @Autowired
    private DocCatalogGroupRepository docCatalogGroupRepository;
    @Autowired
    private DocCatalogRepository docCatalogRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    public Mono<DocCatalogGroupDTO> getById(DocCatalogGroupDetailQueryDTO docCatalogGroupDetailQueryDTO) {
        return docCatalogGroupRepository.findById(docCatalogGroupDetailQueryDTO.getDocCatalogGroupId()).map(o -> {
            DocCatalogGroupDTO docCatalogGroupDTO = new DocCatalogGroupDTO();
            BeanUtils.copyProperties(o, docCatalogGroupDTO);
            return docCatalogGroupDTO;
        });
    }

    public Flux<DocCatalogGroupDTO> getList() {
        return docCatalogGroupRepository.findAll().map(o -> {
            DocCatalogGroupDTO docCatalogGroupDTO = new DocCatalogGroupDTO();
            BeanUtils.copyProperties(o, docCatalogGroupDTO);
            return docCatalogGroupDTO;
        }).sort(Comparator.comparing(DocCatalogGroupDTO::getSort));
    }

    public Mono<Page<DocCatalogGroupDTO>> page(DocCatalogGroupQueryDTO docCatalogGroupQueryDTO, Pageable pageable) {
        Criteria criteria = Criteria.empty();
        if (docCatalogGroupQueryDTO.getStatus() != null) {
            criteria = criteria.and(DocCatalogGroupConstant.STATUS).is(docCatalogGroupQueryDTO.getStatus());
        }
        if (StringUtils.isNotBlank(docCatalogGroupQueryDTO.getName())) {
            criteria = criteria.and(DocCatalogGroupConstant.NAME).like("%" + docCatalogGroupQueryDTO.getName() + "%");
        }

        Query query = Query.query(criteria)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .sort(pageable.getSort());

        return r2dbcEntityTemplate.select(DocCatalogGroup.class)
                .from(DocCatalogGroupConstant.DOC_CATALOG_GROUP)
                .matching(query)
                .all()
                .map(o -> {
                    DocCatalogGroupDTO docCatalogGroupDTO = new DocCatalogGroupDTO();
                    BeanUtils.copyProperties(o, docCatalogGroupDTO);
                    return docCatalogGroupDTO;
                })
                .collectList()
                .zipWith(r2dbcEntityTemplate.count(query, DocCatalogGroup.class))
                .map(p -> new PageImpl<>(p.getT1(), pageable, p.getT2()));
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<DocCatalogGroup> add(DocCatalogGroupAddDTO docCatalogGroupAddDTO) {
        // 校验排序值是否重复
        return docCatalogGroupRepository.countBySort(docCatalogGroupAddDTO.getSort().intValue())
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new BusinessException(ExceptionEnum.SYS_SORT_REPEAT_ERROR));
                    }
                    DocCatalogGroup docCatalogGroup = new DocCatalogGroup();
                    BeanUtils.copyProperties(docCatalogGroupAddDTO, docCatalogGroup);
                    docCatalogGroup.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());
                    docCatalogGroup.setSort(docCatalogGroupAddDTO.getSort().intValue());
                    docCatalogGroup.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                    docCatalogGroup.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    docCatalogGroup.setCreateTime(LocalDateTime.now());
                    docCatalogGroup.setUpdateTime(LocalDateTime.now());
                    return docCatalogGroupRepository.save(docCatalogGroup);
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateById(DocCatalogGroupUpdateDTO docCatalogGroupUpdateDTO) {
        // 校验排序值是否重复
        return docCatalogGroupRepository.countByDocCatalogGroupIdNotAndSort(
                docCatalogGroupUpdateDTO.getDocCatalogGroupId(), docCatalogGroupUpdateDTO.getSort().intValue()
        ).flatMap(count -> {
            if (count > 0) {
                return Mono.error(new BusinessException(ExceptionEnum.SYS_SORT_REPEAT_ERROR));
            }

            // 构建更新参数
            Query query = Query.query(
                    Criteria.where(DocCatalogGroupConstant.DOC_CATALOG_GROUP_ID).is(docCatalogGroupUpdateDTO.getDocCatalogGroupId())
            );
            Update update = Update
                    .update(DocCatalogGroupConstant.NAME, docCatalogGroupUpdateDTO.getName())
                    .set(DocCatalogGroupConstant.DESCRIPTION, docCatalogGroupUpdateDTO.getDescription())
                    .set(DocCatalogGroupConstant.SORT, docCatalogGroupUpdateDTO.getSort())
                    .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                    .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

            // 根据id更新数据
            return r2dbcEntityTemplate.update(query, update, DocCatalogGroup.class);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(DocCatalogGroupDeleteDTO docCatalogGroupDeleteDTO) {
        // 校验分组下是否存在数据
        return docCatalogRepository.countByDocCatalogGroupId(docCatalogGroupDeleteDTO.getDocCatalogGroupId())
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new BusinessException(ExceptionEnum.CATALOG_GROUP_DELETE_ERROR));
                    }
                    return docCatalogGroupRepository.deleteById(docCatalogGroupDeleteDTO.getDocCatalogGroupId());
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatus(DocCatalogGroupStatusUpdateDTO docCatalogGroupStatusUpdateDTO) {
        Query query = Query.query(
                Criteria.where(DocCatalogGroupConstant.DOC_CATALOG_GROUP_ID).is(docCatalogGroupStatusUpdateDTO.getDocCatalogGroupId())
        );
        Update update = Update
                .update(DocCatalogGroupConstant.STATUS, docCatalogGroupStatusUpdateDTO.getStatus())
                .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

        return r2dbcEntityTemplate.update(query, update, DocCatalogGroup.class);
    }

}
