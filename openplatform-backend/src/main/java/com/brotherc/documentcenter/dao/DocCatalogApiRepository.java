package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.DocCatalogApi;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DocCatalogApiRepository extends R2dbcRepository<DocCatalogApi, Long> {

    Mono<DocCatalogApi> findByDocCatalogId(Long docCatalogId);

    Mono<Void> deleteByDocCatalogId(Long docCatalogId);

}
