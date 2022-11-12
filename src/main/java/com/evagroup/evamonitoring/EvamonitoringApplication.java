package com.evagroup.evamonitoring;

import com.evagroup.evamonitoring.server.ServerRepository;
import com.evagroup.evamonitoring.telegram.repository.TelegramUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Duration;

@Slf4j
@SpringBootApplication()
public class EvamonitoringApplication {

	public static void main(String[] args) {
		SpringApplication.run(EvamonitoringApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(ServerRepository serverRepository,
								  TelegramUserRepository telegramUserRepository) {
		return (args -> {
			// fetch all servers
			log.info("Servers found with findAll():");
			log.info("-------------------------------");
			serverRepository.findAll().doOnNext(server -> log.info(server.toString())).blockLast(Duration.ofSeconds(10));

			log.info("");

			// fetch all telegram users
			log.info("Users found with findAll():");
			log.info("-------------------------------");
			telegramUserRepository.findAll().doOnNext(user -> log.info(user.toString())).blockLast(Duration.ofSeconds(10));

			log.info("");
		});
	}
}
