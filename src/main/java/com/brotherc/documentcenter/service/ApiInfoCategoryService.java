package com.brotherc.documentcenter.service;

import com.brotherc.documentcenter.constants.ApiInfoCategoryConstant;
import com.brotherc.documentcenter.constants.DefaultConstant;
import com.brotherc.documentcenter.dao.ApiInfoCategoryRepository;
import com.brotherc.documentcenter.dao.ApiInfoPublishRepository;
import com.brotherc.documentcenter.dao.ApiInfoRepository;
import com.brotherc.documentcenter.enums.ApiInfoCategoryTypeEnum;
import com.brotherc.documentcenter.enums.PublishStatusEnum;
import com.brotherc.documentcenter.exception.BusinessException;
import com.brotherc.documentcenter.exception.ExceptionEnum;
import com.brotherc.documentcenter.helper.ApiInfoHelper;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoDTO;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoQueryDTO;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoSaveDTO;
import com.brotherc.documentcenter.model.dto.apiinfocategory.*;
import com.brotherc.documentcenter.model.entity.ApiInfo;
import com.brotherc.documentcenter.model.entity.ApiInfoCategory;
import com.brotherc.documentcenter.model.entity.ApiInfoPublish;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
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
    private ApiInfoRepository apiInfoRepository;
    @Autowired
    private ApiInfoPublishRepository apiInfoPublishRepository;
    @Autowired
    private R2dbcEntityTemplate r2dbcEntityTemplate;
    @Autowired
    private ApiInfoHelper apiInfoHelper;

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
        return apiInfoCategoryRepository.countByParentIdAndName(addDTO.getParentId(), addDTO.getName())
                .flatMap(count -> {
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

                    // 保存api分类
                    return apiInfoCategoryRepository.save(apiInfoCategory)
                            .flatMap(savedCategory -> {
                                // 如果类型是API
                                if (ApiInfoCategoryTypeEnum.API.getCode() == addDTO.getType()) {
                                    // 则保存API详细信息
                                    ApiInfo apiInfo = new ApiInfo();
                                    apiInfo.setCnName(savedCategory.getName());
                                    apiInfo.setApiInfoCategoryId(savedCategory.getApiInfoCategoryId());
                                    apiInfo.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());
                                    apiInfo.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                                    apiInfo.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                    apiInfo.setCreateTime(LocalDateTime.now());
                                    apiInfo.setUpdateTime(LocalDateTime.now());

                                    return apiInfoRepository.save(apiInfo).thenReturn(savedCategory);
                                } else {
                                    return Mono.just(savedCategory);
                                }
                            });
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

                        return apiInfoCategoryRepository.save(original)
                                .flatMap(savedCategory -> {
                                    // 如果类型是API
                                    if (ApiInfoCategoryTypeEnum.API.getCode() == original.getType()) {
                                        // 则更新API详细信息
                                        return apiInfoRepository.findByApiInfoCategoryId(original.getApiInfoCategoryId())
                                                .flatMap(apiInfo -> {
                                                    apiInfo.setCnName(original.getName());
                                                    apiInfo.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                                    apiInfo.setUpdateTime(LocalDateTime.now());

                                                    return apiInfoRepository.save(apiInfo)
                                                            .flatMap(savedApiInfo -> {
                                                                // 如果存在api发布信息，则更新
                                                                return apiInfoPublishRepository.findByApiInfoId(savedApiInfo.getApiInfoId())
                                                                        .flatMap(publish -> {
                                                                            publish.setCnName(original.getName());
                                                                            publish.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                                                            publish.setUpdateTime(LocalDateTime.now());
                                                                            return apiInfoPublishRepository.save(publish);
                                                                        })
                                                                        .thenReturn(savedCategory)
                                                                        .onErrorResume(e -> Mono.just(savedCategory));
                                                            });
                                                });
                                    } else {
                                        return Mono.just(savedCategory);
                                    }
                                });
                    });
                });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteById(ApiInfoCategoryDeleteDTO deleteDTO) {
        Long categoryId = deleteDTO.getApiInfoCategoryId();

        return apiInfoCategoryRepository.findById(categoryId)
                .switchIfEmpty(Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_UN_EXIST_ERROR)))
                .flatMap(apiInfoCategory ->
                        apiInfoCategoryRepository.countByParentId(categoryId)
                                .flatMap(count -> {
                                    if (count > 0) {
                                        return Mono.error(new BusinessException(ExceptionEnum.SYS_DATA_EXIST_CHILDREN_ERROR));
                                    }

                                    Mono<Void> deleteApiInfoChain = Mono.empty();

                                    // 如果类型是API
                                    if (ApiInfoCategoryTypeEnum.API.getCode() == apiInfoCategory.getType()) {
                                        // 则删除API详细信息和发布信息
                                        deleteApiInfoChain = apiInfoRepository.findByApiInfoCategoryId(categoryId)
                                                .flatMap(apiInfo ->
                                                        Mono.when(
                                                                apiInfoRepository.deleteById(apiInfo.getApiInfoId()),
                                                                apiInfoPublishRepository.deleteByApiInfoId(apiInfo.getApiInfoId())
                                                        )
                                                )
                                                .then();
                                    }

                                    return deleteApiInfoChain.then(apiInfoCategoryRepository.deleteById(categoryId));
                                })
                );
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> deleteByIdList(ApiInfoCategoryBatchDeleteDTO batchDeleteDTO) {
        List<Long> idList = batchDeleteDTO.getApiInfoCategoryIdList();
        Query query = Query.query(Criteria.where(ApiInfoCategoryConstant.API_INFO_CATEGORY_ID).in(idList));

        return Mono.when(
                r2dbcEntityTemplate.delete(query, ApiInfoCategory.class),
                r2dbcEntityTemplate.delete(query, ApiInfo.class),
                r2dbcEntityTemplate.delete(query, ApiInfoPublish.class)
        ).then();
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatus(ApiInfoCategoryStatusUpdateDTO statusUpdateDTO) {
        Long categoryId = statusUpdateDTO.getApiInfoCategoryId();
        Integer newStatus = statusUpdateDTO.getStatus();

        // 更新api分类状态
        Query query = Query.query(Criteria.where(ApiInfoCategoryConstant.API_INFO_CATEGORY_ID).is(categoryId));
        Update update = Update
                .update(ApiInfoCategoryConstant.STATUS, newStatus)
                .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

        Mono<Long> updateCategory = r2dbcEntityTemplate.update(query, update, ApiInfoCategory.class);

        return updateCategory.flatMap(count ->
                apiInfoCategoryRepository.findById(categoryId)
                        .flatMap(category -> {
                            // 如果是api分类，不做后续操作
                            if (ApiInfoCategoryTypeEnum.CATEGORY.getCode() == category.getType()) {
                                return Mono.just(count);
                            }

                            return apiInfoRepository.findByApiInfoCategoryId(categoryId)
                                    .flatMap(apiInfo -> {
                                        // 更新api信息状态
                                        apiInfo.setStatus(newStatus);
                                        apiInfo.setUpdateTime(LocalDateTime.now());
                                        apiInfo.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);

                                        Mono<Void> syncPublishTable;

                                        if (PublishStatusEnum.PUBLISH.getCode() == newStatus) {
                                            // 发布：先删除api发布信息，再插入新记录
                                            ApiInfoPublish publish = new ApiInfoPublish();
                                            BeanUtils.copyProperties(apiInfo, publish);

                                            publish.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                                            publish.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                            publish.setCreateTime(LocalDateTime.now());
                                            publish.setUpdateTime(LocalDateTime.now());

                                            syncPublishTable = apiInfoPublishRepository
                                                    .deleteByApiInfoId(apiInfo.getApiInfoId())
                                                    .then(apiInfoPublishRepository.save(publish))
                                                    .then();
                                        } else {
                                            // 取消发布：仅删除api发布信息
                                            syncPublishTable = apiInfoPublishRepository
                                                    .deleteByApiInfoId(apiInfo.getApiInfoId());
                                        }

                                        return apiInfoRepository.save(apiInfo)
                                                .then(syncPublishTable)
                                                .thenReturn(count);
                                    });
                        })
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Long> updateStatusBatch(ApiInfoCategoryStatusBatchUpdateDTO statusUpdateDTO) {
        List<Long> categoryIdList = statusUpdateDTO.getApiInfoCategoryIdList();
        Integer newStatus = statusUpdateDTO.getStatus();

        // 批量更新api分类状态
        Query query = Query.query(Criteria.where(ApiInfoCategoryConstant.API_INFO_CATEGORY_ID).in(categoryIdList));
        Update update = Update
                .update(ApiInfoCategoryConstant.STATUS, newStatus)
                .set(DefaultConstant.UPDATE_TIME, LocalDateTime.now())
                .set(DefaultConstant.UPDATE_BY, DefaultConstant.DEFAULT_UPDATE_BY);

        return r2dbcEntityTemplate.update(query, update, ApiInfoCategory.class)
                .flatMap(updatedCount ->
                        // 查找所有api信息
                        apiInfoCategoryRepository.findAllById(categoryIdList)
                                .filter(cat -> ApiInfoCategoryTypeEnum.API.getCode() == cat.getType())
                                .collectList()
                                .flatMap(type2Categories -> {
                                    if (type2Categories.isEmpty()) {
                                        return Mono.just(updatedCount);
                                    }

                                    List<Long> type2CategoryIds = type2Categories.stream()
                                            .map(ApiInfoCategory::getApiInfoCategoryId)
                                            .toList();

                                    return apiInfoRepository.findAllByApiInfoCategoryIdIn(type2CategoryIds)
                                            .collectList()
                                            .flatMap(apiInfos -> {
                                                // 批量更新api信息的状态
                                                for (ApiInfo apiInfo : apiInfos) {
                                                    apiInfo.setStatus(newStatus);
                                                    apiInfo.setUpdateTime(LocalDateTime.now());
                                                    apiInfo.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                                }

                                                Mono<Void> updateApiInfos = apiInfoRepository.saveAll(apiInfos).then();

                                                Mono<Void> syncPublish = Flux.fromIterable(apiInfos)
                                                        .flatMap(apiInfo -> {
                                                            if (PublishStatusEnum.PUBLISH.getCode() == newStatus) {
                                                                ApiInfoPublish publish = new ApiInfoPublish();
                                                                BeanUtils.copyProperties(apiInfo, publish);

                                                                publish.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                                                                publish.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                                                publish.setCreateTime(LocalDateTime.now());
                                                                publish.setUpdateTime(LocalDateTime.now());

                                                                return apiInfoPublishRepository.deleteByApiInfoId(apiInfo.getApiInfoId())
                                                                        .then(apiInfoPublishRepository.save(publish))
                                                                        .then();
                                                            } else {
                                                                return apiInfoPublishRepository.deleteByApiInfoId(apiInfo.getApiInfoId());
                                                            }
                                                        })
                                                        .then();

                                                return updateApiInfos.then(syncPublish).thenReturn(updatedCount);
                                            });
                                })
                );
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<ApiInfo> save(@Valid ApiInfoSaveDTO addDTO) {
        Long categoryId = addDTO.getApiInfoCategoryId();

        return apiInfoRepository.findByApiInfoCategoryId(categoryId)
                .flatMap(existing -> {
                    // 存在 -> 更新
                    ApiInfo apiInfo = apiInfoHelper.generateApiInfo(existing, addDTO);
                    apiInfo.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                    apiInfo.setUpdateTime(LocalDateTime.now());
                    return apiInfoRepository.save(existing);
                })
                .switchIfEmpty(
                        // 不存在 -> 新增
                        Mono.defer(() -> {
                            ApiInfo apiInfo = apiInfoHelper.generateApiInfo(null, addDTO);
                            apiInfo.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                            apiInfo.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                            apiInfo.setCreateTime(LocalDateTime.now());
                            apiInfo.setUpdateTime(LocalDateTime.now());
                            apiInfo.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());
                            return apiInfoRepository.save(apiInfo);
                        })
                )
                .flatMap(savedApi -> {
                    // 继续处理api发布信息
                    return apiInfoPublishRepository.findByApiInfoCategoryId(categoryId)
                            .flatMap(existingPub -> {
                                // 存在 -> 更新
                                BeanUtils.copyProperties(addDTO, existingPub);
                                existingPub.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                existingPub.setUpdateTime(LocalDateTime.now());
                                return apiInfoPublishRepository.save(existingPub);
                            })
                            .thenReturn(savedApi);
                });

    }

    public Mono<ApiInfoDTO> getByApiInfoCategoryId(ApiInfoQueryDTO queryDTO) {
        return apiInfoRepository.findByApiInfoCategoryId(queryDTO.getApiInfoCategoryId())
                .map(apiInfo -> apiInfoHelper.generateApiInfoDTO(apiInfo));
    }

}
