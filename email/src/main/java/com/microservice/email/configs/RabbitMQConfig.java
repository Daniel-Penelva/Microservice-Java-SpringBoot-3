package com.microservice.email.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${broker.queue.email.name}")
    private String queue;

    /** Esse método representa uma fila RabbitMQ.*/
    @Bean
    public Queue queue(){
        return new Queue(queue, true);
    }

    /**
     * Esse método será usada para converter objetos Java em mensagens JSON ao enviar mensagens para RabbitMQ e vice-versa ao receber mensagens da fila.*/
    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        ObjectMapper objectMapper = new ObjectMapper();

        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
