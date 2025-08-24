package com.brotherc.documentcenter.dao;

import com.brotherc.documentcenter.model.entity.User;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {

    Mono<Long> countByUsernameAndIsDel(String username, Integer isDel);

    Mono<User> findByUsernameAndIsDel(String username, Integer isDel);

    Mono<User> findByUserIdAndIsDel(Long userId, Integer isDel);

}
