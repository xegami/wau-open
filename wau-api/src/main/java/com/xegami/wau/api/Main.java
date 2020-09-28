package com.xegami.wau.api;

import com.xegami.wau.api.service.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.dozer.DozerBeanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@SpringBootApplication
public class Main {

    @Autowired
    RoomService roomService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public DozerBeanMapper mapper() {
        return new DozerBeanMapper();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onAppReady() {
        roomService.loadRooms();
        roomService.refreshQuestions();

        log.info("Servidor listo.");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}



