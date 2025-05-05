package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.DocCatalog;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DocCatalogRepository extends R2dbcRepository<DocCatalog, Long> {

    Flux<DocCatalog> findByDocCatalogGroupId(Long docCatalogGroupId);

    Mono<Long> countByDocCatalogGroupIdAndParentIdAndSort(Long docCatalogGroupId, Long parentId, Integer sort);

    Mono<Long> countByParentId(Long parentId);

    Mono<Long> countByDocCatalogGroupId(Long docCatalogGroupId);

}
