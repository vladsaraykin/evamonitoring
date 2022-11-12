package com.evagroup.evamonitoring.telegram.config;

import com.evagroup.evamonitoring.server.ServerRepository;
import com.evagroup.evamonitoring.telegram.Bot;
import com.evagroup.evamonitoring.telegram.repository.TelegramUserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfig {

    @Bean
    public Bot telegramBot(TelegramUserRepository telegramUserRepository,
                           ServerRepository serverRepository,
                           @Value("${telegram.name}") String botName,
                           @Value("${telegram.token}") String token) {
        return new Bot(new DefaultBotOptions(), botName, token, telegramUserRepository, serverRepository);
    }

    @Bean
    @SneakyThrows
    public TelegramBotsApi telegramBotsApi(Bot bot) {
        final TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(bot);
        return telegramBotsApi;
    }
}
