package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.DocCatalogGroup;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DocCatalogGroupRepository extends R2dbcRepository<DocCatalogGroup, Long> {

    Mono<Long> countBySort(int sort);

    Mono<Long> countByDocCatalogGroupIdNotAndSort(Long docCatalogGroupId, Integer sort);

    @Query("SELECT COALESCE(MAX(sort), 0) FROM doc_catalog_group")
    Mono<Integer> findMaxSort();

}
