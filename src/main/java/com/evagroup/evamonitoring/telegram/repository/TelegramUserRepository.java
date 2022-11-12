package com.evagroup.evamonitoring.telegram.repository;

import com.evagroup.evamonitoring.telegram.entity.TelegramStatus;
import com.evagroup.evamonitoring.telegram.entity.TelegramUser;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TelegramUserRepository extends ReactiveCrudRepository<TelegramUser, Long> {

    Mono<TelegramUser> findByUserId(Long userid);

    @Modifying
    @Query("UPDATE telegram_user tgu set tgu.status = :kicked where tgu.user_id = :userId")
    Mono<TelegramUser> updateStatus(Long userId, TelegramStatus kicked);
}
