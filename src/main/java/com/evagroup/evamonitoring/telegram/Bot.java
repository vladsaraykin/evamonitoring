package com.evagroup.evamonitoring.telegram;

import com.evagroup.evamonitoring.server.Server;
import com.evagroup.evamonitoring.server.ServerRepository;
import com.evagroup.evamonitoring.telegram.entity.TelegramStatus;
import com.evagroup.evamonitoring.telegram.entity.TelegramUser;
import com.evagroup.evamonitoring.telegram.repository.TelegramUserRepository;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class Bot extends TelegramLongPollingBot {

    private final String botName;
    private final String token;
    private final TelegramUserRepository telegramUserRepository;
    private final ServerRepository serverRepository;

    public Bot(DefaultBotOptions options, String botName, String token, TelegramUserRepository telegramUserRepository, ServerRepository serverRepository) {
        super(options);
        this.botName = botName;
        this.token = token;
        this.telegramUserRepository = telegramUserRepository;
        this.serverRepository = serverRepository;
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery());
        } else if (update.hasMyChatMember()) {
            final ChatMember newChatMember = update.getMyChatMember().getNewChatMember();
            if (newChatMember.getStatus().equals("kicked")) {
                log.info("User {} has been kicked", update.getMyChatMember().getFrom().getUserName());
                telegramUserRepository.updateStatus(update.getMyChatMember().getFrom().getId(), TelegramStatus.KICKED).subscribe();
            }
        }
    }

    @SneakyThrows
    private void handleCallback(CallbackQuery callbackQuery) {
        final Message message = callbackQuery.getMessage();
        final String data = callbackQuery.getData();
        final String[] params = data.split(":");
        switch (params[0]) {
            case "DELETE" -> {
                serverRepository.deleteById(Long.valueOf(params[1]))
                        .doOnSuccess(buttons -> updateMessage(getKeyboards(), message))
                        .doOnSuccess(s -> updateMessage(getKeyboards(), message))
                        .subscribe();

            }
            case "ADD_SERVER_STEP1" ->
                executeAsync(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text("Введите АПИ сервера для проверки: ")
                        .disableNotification(true)
                        .build());
        }
    }

    @SneakyThrows
    private void handleMessage(Message message) {
        //handle command
        if (message.hasText() && message.hasEntities()) {
            final Optional<MessageEntity> commandEntity = message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();
            if (commandEntity.isPresent()) {
                final String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
                switch (command) {
                    case "/get_all_healthcheck_api" ->  getKeyboards().doOnSuccess(buttons -> {
                                try {
                                    execute(SendMessage.builder()
                                            .chatId(message.getChatId())
                                            .text("АПИ серверов")
                                            .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons)
                                                    .keyboardRow(List.of(InlineKeyboardButton.builder().text("Добавить").callbackData("ADD_SERVER_STEP1:").build())).build())
                                            .disableNotification(true)
                                            .disableWebPagePreview(true)
                                            .build());
                                } catch (TelegramApiException e) {
                                    log.error("Couldn't sent message to user {}. Message: {}", message.getFrom().getUserName(), message);
                                }
                            })
                            .subscribe();
                    case "/start" -> telegramUserRepository.findByUserId(message.getFrom().getId())
                            .doOnNext(user -> {
                                if (TelegramStatus.KICKED == user.status()) {
                                    telegramUserRepository.updateStatus(user.userId(), TelegramStatus.MEMBER).subscribe();
                                    log.info("User {} had status {}. Update status to {}", user.username(), TelegramStatus.KICKED, TelegramStatus.MEMBER);
                                }
                            })
                            .switchIfEmpty(
                                    telegramUserRepository.save(new TelegramUser(message.getFrom().getId(), message.getFrom().getUserName(), TelegramStatus.MEMBER))
                                            .doOnNext(user -> log.info("{} hase been registered", user.username()))
                            )
                            .doOnNext(user -> {
                                log.info("User with id {} has already registered", user.userId());
                                try {
                                    executeAsync(SendMessage.builder()
                                            .chatId(message.getChatId())
                                            .text("You are registered!")
                                            .build());
                                } catch (TelegramApiException e) {
                                    log.error("Couldn't sent message to user " + message.getFrom().getUserName());
                                }
                            })
                            .subscribe();
                }
            }
            if (message.getEntities().stream().anyMatch(e -> "url".equals(e.getType()))) {
                serverRepository.save(new Server(message.getText()))
                        .doOnSuccess(s -> {
                            log.info("Server url {} has been registered", message.getText());
                            updateMessage(getKeyboards(), message);
                        }).subscribe();
            }

        }
    }

    private void updateMessage(Mono<ArrayList<List<InlineKeyboardButton>>> buttonsPublisher, Message message) {
        buttonsPublisher.doOnSuccess(buttons -> {
            try {
                executeAsync(EditMessageText.builder()
                        .chatId(message.getChatId())
                        .messageId(message.getMessageId())
                        .replyMarkup(InlineKeyboardMarkup.builder().keyboard(buttons).build())
                        .text(message.getText())
                        .build());
            } catch (TelegramApiException e) {
                log.error("Couldn't sent message to user {}. Message: {}", message.getFrom().getUserName(), message);
            }
        }).subscribe();
    }

    private Mono<ArrayList<List<InlineKeyboardButton>>> getKeyboards() {
         return serverRepository.findAll()
                .reduce(new ArrayList<>(), (list, server) -> {
                    list.add(List.of(
                            InlineKeyboardButton.builder().text(server.url()).callbackData("data:" + server.id()).build(),
                            InlineKeyboardButton.builder().text("Удалить ❌").callbackData("DELETE:" + server.id()).build()
                    ));
                    return list;
                });
    }
}
