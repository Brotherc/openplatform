package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.ApiInfoCategory;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface ApiInfoCategoryRepository extends R2dbcRepository<ApiInfoCategory, Long> {

    Mono<Long> countByParentIdAndName(Long parentId, String name);

    Mono<Long> countByParentId(Long parentId);

}
