package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.ApiInfoPublish;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ApiInfoPublishRepository extends R2dbcRepository<ApiInfoPublish, Long> {

    Mono<ApiInfoPublish> findByApiInfoId(Long apiInfoId);

    Mono<Void> deleteByApiInfoId(Long apiInfoId);

}
