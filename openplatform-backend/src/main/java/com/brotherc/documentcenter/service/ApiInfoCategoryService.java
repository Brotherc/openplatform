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
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoImportDTO;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoQueryDTO;
import com.brotherc.documentcenter.model.dto.apiinfo.ApiInfoSaveDTO;
import com.brotherc.documentcenter.model.dto.apiinfocategory.*;
import com.brotherc.documentcenter.model.entity.ApiInfo;
import com.brotherc.documentcenter.model.entity.ApiInfoCategory;
import com.brotherc.documentcenter.model.entity.ApiInfoPublish;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.openapi2apischema.core.ApiSchemaGenerator;
import com.github.openapi2apischema.core.enums.OpenApiVersion;
import com.github.openapi2apischema.core.model.ApiSchema;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    @Autowired
    private WebClient.Builder webClientBuilder;
    @Autowired
    private ObjectMapper objectMapper;

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
                            .then(
                                    // 更新api分类
                                    apiInfoCategoryRepository.findById(categoryId)
                                            .flatMap(category -> {
                                                category.setName(addDTO.getCnName());
                                                category.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                                category.setUpdateTime(LocalDateTime.now());
                                                return apiInfoCategoryRepository.save(category);
                                            })
                            )
                            .thenReturn(savedApi);
                });

    }

    public Mono<ApiInfoDTO> getByApiInfoCategoryId(ApiInfoQueryDTO queryDTO) {
        return apiInfoRepository.findByApiInfoCategoryId(queryDTO.getApiInfoCategoryId())
                .map(apiInfo -> apiInfoHelper.generateApiInfoDTO(apiInfo));
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<Void> importApiInfo(ApiInfoImportDTO importDTO) {
        // 1. 通过URL获取Swagger JSON数据
        return fetchSwaggerJson(importDTO.getUrl())
                .flatMap(json -> {
                    try {
                        // 2. 解析Swagger JSON生成ApiSchema列表
                        List<ApiSchema> apiSchemas = ApiSchemaGenerator.generateBySwaggerJson(OpenApiVersion.V2, json);

                        // 3. 获取所有的tags
                        Set<String> allTags = apiSchemas.stream()
                                .flatMap(schema -> schema.getTags().stream())
                                .collect(Collectors.toSet());

                        // 4. 处理tags（创建或查找api分类）
                        return processTagsAndCreateCategories(allTags)
                                .flatMap(tagCategoryMap -> {
                                    // 5. 处理每个ApiSchema
                                    return processApiSchemas(apiSchemas, tagCategoryMap);
                                });

                    } catch (Exception e) {
                        log.error("解析Swagger JSON失败", e);
                        return Mono.error(new BusinessException(ExceptionEnum.SYS_ERROR));
                    }
                })
                .then();
    }

    /**
     * 通过URL获取Swagger JSON数据
     */
    private Mono<String> fetchSwaggerJson(String url) {
        return webClientBuilder.build()
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .onErrorMap(e -> {
                    log.error("获取Swagger JSON失败，URL: {}", url, e);
                    return new BusinessException(ExceptionEnum.SYS_ERROR);
                });
    }

    /**
     * 处理tags，创建或查找对应的api分类
     */
    private Mono<Map<String, Long>> processTagsAndCreateCategories(Set<String> tags) {
        return Flux.fromIterable(tags)
                .flatMap(tag ->
                    // 查找是否存在该tag对应的分类
                    apiInfoCategoryRepository.findByName(tag)
                            .switchIfEmpty(
                                // 不存在则创建
                                Mono.defer(() -> {
                                    ApiInfoCategory category = new ApiInfoCategory();
                                    category.setName(tag);
                                    category.setParentId(0L);
                                    category.setType(ApiInfoCategoryTypeEnum.CATEGORY.getCode());
                                    category.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());
                                    category.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                                    category.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                                    category.setCreateTime(LocalDateTime.now());
                                    category.setUpdateTime(LocalDateTime.now());
                                    return apiInfoCategoryRepository.save(category);
                                })
                            )
                            .map(category -> Map.entry(tag, category.getApiInfoCategoryId()))
                )
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }

    /**
     * 处理ApiSchema列表，创建或更新api
     */
    private Mono<Void> processApiSchemas(List<ApiSchema> apiSchemas, Map<String, Long> tagCategoryMap) {
        return Flux.fromIterable(apiSchemas)
                .flatMap(apiSchema -> {
                    // 只取第一个tag创建API分类和API信息
                    if (apiSchema.getTags() == null || apiSchema.getTags().isEmpty()) {
                        log.warn("ApiSchema没有tags，跳过处理: {}", apiSchema.getPath());
                        return Mono.empty();
                    }

                    String firstTag = apiSchema.getTags().get(0);
                    Long parentCategoryId = tagCategoryMap.get(firstTag);
                    if (parentCategoryId == null) {
                        log.warn("未找到tag对应的分类: {}", firstTag);
                        return Mono.empty();
                    }

                    return processApiSchema(apiSchema, parentCategoryId);
                })
                .then();
    }

    /**
     * 处理单个ApiSchema
     */
    private Mono<Void> processApiSchema(ApiSchema apiSchema, Long parentCategoryId) {
        return apiInfoRepository.findByCode(apiSchema.getCode())
                .flatMap(existingApi -> {
                    // 存在则更新
                    return updateExistingApi(existingApi, apiSchema);
                })
                .switchIfEmpty(
                    // 不存在则创建
                    Mono.defer(() -> createNewApi(apiSchema, parentCategoryId))
                )
                .then();
    }

    /**
     * 更新已存在的API
     */
    private Mono<ApiInfo> updateExistingApi(ApiInfo existingApi, ApiSchema apiSchema) {
        try {
            // 更新API信息
            updateApiInfoFromSchema(existingApi, apiSchema);
            existingApi.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
            existingApi.setUpdateTime(LocalDateTime.now());

            return apiInfoRepository.save(existingApi);
        } catch (Exception e) {
            log.error("更新API信息失败，code: {}", existingApi.getCode(), e);
            return Mono.error(new BusinessException(ExceptionEnum.SYS_UPDATE_ERROR));
        }
    }

    /**
     * 创建API
     */
    private Mono<ApiInfo> createNewApi(ApiSchema apiSchema, Long parentCategoryId) {
        return createApiCategory(apiSchema.getCnName(), parentCategoryId)
                .flatMap(apiCategory -> {
                    try {
                        // 创建API信息
                        ApiInfo apiInfo = new ApiInfo();
                        apiInfo.setName(apiSchema.getName());
                        apiInfo.setDescription(apiSchema.getDescription());
                        apiInfo.setCnName(apiSchema.getCnName());
                        apiInfo.setCode(apiSchema.getCode());
                        apiInfo.setApiInfoCategoryId(apiCategory.getApiInfoCategoryId());
                        apiInfo.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());

                        updateApiInfoFromSchema(apiInfo, apiSchema);

                        apiInfo.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
                        apiInfo.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
                        apiInfo.setCreateTime(LocalDateTime.now());
                        apiInfo.setUpdateTime(LocalDateTime.now());

                        return apiInfoRepository.save(apiInfo);
                    } catch (Exception e) {
                        log.error("创建API信息失败，code: {}", apiSchema.getCode(), e);
                        return Mono.error(new BusinessException(ExceptionEnum.SYS_SAVE_ERROR));
                    }
                });
    }

    /**
     * 创建API分类
     */
    private Mono<ApiInfoCategory> createApiCategory(String apiName, Long parentCategoryId) {
        ApiInfoCategory apiCategory = new ApiInfoCategory();
        apiCategory.setName(apiName);
        apiCategory.setParentId(parentCategoryId);
        apiCategory.setType(ApiInfoCategoryTypeEnum.API.getCode());
        apiCategory.setStatus(PublishStatusEnum.UN_PUBLISH.getCode());
        apiCategory.setCreateBy(DefaultConstant.DEFAULT_CREATE_BY);
        apiCategory.setUpdateBy(DefaultConstant.DEFAULT_UPDATE_BY);
        apiCategory.setCreateTime(LocalDateTime.now());
        apiCategory.setUpdateTime(LocalDateTime.now());

        return apiInfoCategoryRepository.save(apiCategory);
    }

    /**
     * 根据ApiSchema更新api信息
     */
    private void updateApiInfoFromSchema(ApiInfo apiInfo, ApiSchema apiSchema) throws Exception {
        apiInfo.setReqMethod(apiSchema.getMethod());
        apiInfo.setReqContextPath(apiSchema.getBasePath());
        apiInfo.setReqPath(apiSchema.getPath());

        // 处理请求参数
        if (apiSchema.getParameters() != null) {
            apiInfo.setReqParam(objectMapper.writeValueAsString(apiSchema.getParameters()));
        }

        if (apiSchema.getDisplayParameters() != null) {
            apiInfo.setReqParamDisplay(objectMapper.writeValueAsString(apiSchema.getDisplayParameters()));
        }

        // 处理响应体
        if (apiSchema.getResponses() != null) {
            apiInfo.setReturnInfo(objectMapper.writeValueAsString(apiSchema.getResponses()));
        }

        if (apiSchema.getDisplayResponses() != null) {
            apiInfo.setReturnInfoDisplay(objectMapper.writeValueAsString(apiSchema.getDisplayResponses()));
        }
    }

}
