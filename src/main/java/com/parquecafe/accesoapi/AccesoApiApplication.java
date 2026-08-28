package com.parquecafe.accesoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // necesario para que corran las tareas @Scheduled como  limpieza del historial
public class AccesoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccesoApiApplication.class, args);
    }

}
