package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.DocCatalogGroup;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocCatalogGroupRepository extends R2dbcRepository<DocCatalogGroup, Long> {

}
