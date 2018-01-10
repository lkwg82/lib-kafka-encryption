package de.idealo.kafka.demo;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
public class DemoApp {
    public static void main(String... args) {
        new SpringApplicationBuilder(DemoApp.class).run(args);
    }

    @Service
    @EnableKafka
    public static class Consumer {
        @Getter
        private final BlockingQueue<String> receivedMessages = new ArrayBlockingQueue<>(1);

        @KafkaListener(topics = "test-topic")
        void consume(String message) {
            receivedMessages.add(message);
        }
    }

    @Service
    @RequiredArgsConstructor
    static class Producer {
        private final KafkaTemplate<String, String> template;

        void send(String message) {
            template.send("test-topic", message);
            template.flush();
        }
    }
}
