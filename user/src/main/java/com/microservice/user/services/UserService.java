package com.microservice.user.services;

import com.microservice.user.models.UserModel;
import com.microservice.user.repositories.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

   final UserRepository userRepository;

   public UserService(UserRepository userRepository){
       this.userRepository = userRepository;
   }

   /**
    * A anotação @Transaction têm como objetivos a precaução para gerar o rollback entre as operações.
    * (1) Operação de salvar o user.
    * (2) Operação que irá publicar a mensagem que vai ser gerada pelo canal via Broker.
    * Ou seja, se uma das operações falhar é gerado o rollback*/
    @Transactional
    public UserModel save(UserModel userModel){
        return userRepository.save(userModel);
    }
}
