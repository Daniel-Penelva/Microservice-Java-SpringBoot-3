package com.microservice.user.producers;

import com.microservice.user.dtos.EmailDto;
import com.microservice.user.models.UserModel;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserProducer {

    final RabbitTemplate rabbitTemplate;

    /**
     * RabbitTemplate é facilitar o envio de mensagens para filas ou exchanges no RabbitMQ. Isso é feito pelo método convertAndSend, que converte o
     * objeto fornecido em uma mensagem e a envia para a fila ou exchange especificada.*/
    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // exchange do tipo default: a chave routing key é o mesmo nome da fila queue, ou seja, vai receber o valor "default.email" na variável "routingKey".
    @Value(value = "${broker.queue.email.name}")
    private String routingKey;

    // Esse método publishMessageEmail é responsável por publicar uma mensagem na fila RabbitMQ associada a uma troca (exchange).
    public void publishMessageEmail(UserModel userModel){
        var emailDto = new EmailDto();

        emailDto.setUserId(userModel.getUserId());
        emailDto.setEmailTo(userModel.getEmail());
        emailDto.setSubject("Cadastro realizado com sucesso!");
        emailDto.setText(userModel.getName() + ", seja bem vindo(a)! Agradecemos o seu cadastro.");

        // passando somente aspas na exchange já fica entendido que vai ser uma exchange default.
        rabbitTemplate.convertAndSend("", routingKey, emailDto);
    }
}
