package com.microservice.email.consumers;

import com.microservice.email.dtos.EmailRecordDto;
import com.microservice.email.models.EmailModel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class EmailConsumer {

    @RabbitListener(queues = "${broker.queue.email.name}")
    public void listenEmailQueue(@Payload EmailRecordDto emailRecordDto){

        //System.out.println(emailRecordDto.emailTo());

        var emailModel = new EmailModel();

        // Converter o emailRecordDto para emailModel
        BeanUtils.copyProperties(emailRecordDto, emailModel);
    }
}
