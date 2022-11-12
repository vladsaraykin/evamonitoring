package com.evagroup.evamonitoring.telegram;

import com.evagroup.evamonitoring.NotificationService;
import com.evagroup.evamonitoring.telegram.entity.TelegramStatus;
import com.evagroup.evamonitoring.telegram.repository.TelegramUserRepository;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
@AllArgsConstructor
public class TelegramNotification implements NotificationService {

    private final Bot bot;
    private final TelegramUserRepository telegramUserRepository;

    @Override
    @SneakyThrows
    public Mono<String> sendNotification(String msg) {
        log.error(msg);

        return telegramUserRepository.findAll().cache(Duration.ofSeconds(30))
                .map(user -> {
                    if (user.status() == TelegramStatus.KICKED) {
                        log.info("User {} is kicked", user.username());
                        return Mono.empty();
                    }
                    try {
                        return Mono.fromFuture(bot.executeAsync(SendMessage.builder().chatId(user.userId()).text(msg).build()));
                    } catch (TelegramApiException e) {
                        return Mono.error(new Exception("Can't send message to user " + user.userId()));
                    }
                }).reduce("Notify all subscribed users", (start, mono) -> start);
    }
}
