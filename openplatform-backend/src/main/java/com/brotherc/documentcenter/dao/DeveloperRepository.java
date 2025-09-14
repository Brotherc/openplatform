package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.Developer;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DeveloperRepository extends R2dbcRepository<Developer, Long> {

    Mono<Long> countByUsernameAndIsDel(String username, Integer isDel);

    Mono<Developer> findByUsernameAndIsDel(String username, Integer isDel);

    Mono<Developer> findByDeveloperIdAndIsDel(Long developerId, Integer isDel);

}
