package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.Menu;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface MenuRepository extends R2dbcRepository<Menu, Long> {

    Mono<Long> countByParentIdAndSort(Long parentId, Integer sort);

    Mono<Long> countByParentId(Long parentId);

}
