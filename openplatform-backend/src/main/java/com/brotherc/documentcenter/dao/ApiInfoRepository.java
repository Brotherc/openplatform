package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.ApiInfo;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface ApiInfoRepository extends R2dbcRepository<ApiInfo, Long> {

    Mono<ApiInfo> findByApiInfoCategoryId(Long apiInfoCategoryId);

    Flux<ApiInfo> findAllByApiInfoCategoryIdIn(List<Long> apiInfoCategoryIdList);

}
