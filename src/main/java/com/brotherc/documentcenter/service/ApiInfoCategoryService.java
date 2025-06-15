package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.constants.ApiInfoCategoryConstant;
import com.brotherc.documentcenter.constants.DefaultConstant;
import com.brotherc.documentcenter.dao.ApiInfoCategoryRepository;
import com.brotherc.documentcenter.enums.PublishStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.model.dto.apiinfocategory.*;
import com.brotherc.documentcenter.model.entity.ApiInfoCategory;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
public class ApiInfoCategoryService {

    @Autowired
    private ApiInfoCategoryRepository apiInfoCategoryRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;

    public Mono<List<ApiInfoCategoryNodeDTO>> getTree() {
        return apiInfoCategoryRepository.findAll()
                .collectList()
                .map(o -> buildCatalogTree(o, 0L));
    }

    private List<ApiInfoCategoryNodeDTO> buildCatalogTree(List<ApiInfoCategory> all, Long parentId) {
        List<ApiInfoCategoryNodeDTO> result = new ArrayList<>();

        // 排序
        all.sort(Comparator.comparing(ApiInfoCategory::getApiInfoCategoryId, Comparator.nullsLast(Long::compareTo)));

        List<ApiInfoCategoryNodeDTO> copyList = all.stream().map(o -> {
            ApiInfoCategoryNodeDTO apiInfoCategoryNodeDTO = new ApiInfoCategoryNodeDTO();
            BeanUtils.copyProperties(o, apiInfoCategoryNodeDTO);
            return apiInfoCategoryNodeDTO;
        }).toList();

        // 设置子节点
        for (ApiInfoCategoryNodeDTO item : copyList) {
            if (parentId.equals(item.getParentId())) {
                item.setChildren(buildCatalogTree(all, item.getApiInfoCategoryId()));
                result.add(item);
            }
        }

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<ApiInfoCategory> add(ApiInfoCategoryAddDTO addDTO) {
        return apiInfoCategoryRepository.countByParentIdAndName(addDTO.getParentId(), addDTO.getName()).flatMap(count -> {
            if (count > 0) {
                return Mono.error(new BusinessException(ExceptionEnum.API_INFO_CATEGORY_NAME_EXIST_ERROR));
            }
            ApiInfoCategory apiInfoCategory = new ApiInfoCategory();
            BeanUtils.copyProperties(addDTO, apiInfoCategory);
            apiInfoCategory.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());
            apiInfoCategory.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
            apiInfoCategory.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
            apiInfoCategory.setCreateTime(LocalDateTime.now());
            apiInfoCategory.setUpdateTime(LocalDateTime.now());
            return apiInfoCategoryRepository.save(apiInfoCategory);
        });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<ApiInfoCategory> updateById(ApiInfoCategoryUpdateDTO updateDTO) {
        return apiInfoCategoryRepository.findById(updateDTO.getApiInfoCategoryId())
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(original -> {
                    boolean nameChange = !Objects.equals(original.getParentId(), updateDTO.getParentId()) ||
                            !Objects.equals(updateDTO.getName(), original.getName());
                    Mono<Boolean> nameRepeat = nameChange ? apiInfoCategoryRepository.countByParentIdAndName(
                            updateDTO.getParentId(), updateDTO.getName()
                    ).map(count -> count > 0) : Mono.just(false);

                    return nameRepeat.flatMap(repeat -> {
                        if (Objects.equals(true, repeat)) {
                            return Mono.error(new BusinessException(ExceptionEnum.API_INFO_CATEGORY_NAME_EXIST_ERROR));
                        }

                        original.setName(updateDTO.getName());
                        original.setParentId(updateDTO.getParentId());
                        original.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                        original.setUpdateTime(LocalDateTime.now());

                        return apiInfoCategoryRepository.save(original);
                    });
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(ApiInfoCategoryDeleteDTO deleteDTO) {
        return apiInfoCategoryRepository.findById(deleteDTO.getApiInfoCategoryId())
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(apiInfoCategory ->
                        apiInfoCategoryRepository.countByParentId(deleteDTO.getApiInfoCategoryId())
                                .flatMap(count -> {
                                    if (count > 0) {
                                        return Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_EXIST_CHILDREN_ERROR));
                                    }

                                    return apiInfoCategoryRepository.deleteById(deleteDTO.getApiInfoCategoryId());
                                })
                );
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteByIdList(ApiInfoCategoryBatchDeleteDTO batchDeleteDTO) {
        Query query = Query.query(Criteria.where(ApiInfoCategoryConstant.API_INFO_CATEGORY_ID).in(batchDeleteDTO.getApiInfoCategoryIdList()));
        return r2dbcEntityTemplate
                .delete(query, ApiInfoCategory.class)
                .then();
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatus(ApiInfoCategoryStatusUpdateDTO statusUpdateDTO) {
        Query query = Query.query(Criteria.where(ApiInfoCategoryConstant.API_INFO_CATEGORY_ID).is(statusUpdateDTO.getApiInfoCategoryId()));
        Update update = Update
                .update(ApiInfoCategoryConstant.STATUS, statusUpdateDTO.getStatus())
                .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

        return r2dbcEntityTemplate.update(query, update, ApiInfoCategory.class);
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatusBatch(ApiInfoCategoryStatusBatchUpdateDTO statusUpdateDTO) {
        Query query = Query.query(Criteria.where(ApiInfoCategoryConstant.API_INFO_CATEGORY_ID).in(statusUpdateDTO.getApiInfoCategoryIdList()));
        Update update = Update
                .update(ApiInfoCategoryConstant.STATUS, statusUpdateDTO.getStatus())
                .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

        return r2dbcEntityTemplate.update(query, update, ApiInfoCategory.class);
    }

}
