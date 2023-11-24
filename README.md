# Introdução

## **Desenvolvimento de Microserviços de Usuário e E-mail com Comunicação Assíncrona**

### 1. **`user-microservice` - Gerenciamento de Usuários:**
   O `user-microservice` será responsável por gerenciar todas as operações relacionadas aos usuários, bem como o cadastro de informações do usuário. 

### 2. **`email-microservice` - Serviço de E-mails:**
   O `email-microservice` terá como foco o envio de e-mails em resposta a determinados eventos, como o cadastro de um novo usuário. Vai ser utilizado a interface `JavaMailSender` para a integração com o SMTP do Gmail, garantindo uma comunicação confiável e segura.

### 3. **Comunicação Assíncrona com RabbitMQ:**
   Para facilitar a comunicação entre os microserviços vai ser implementado uma arquitetura de mensageria assíncrona utilizando o RabbitMQ. Essa abordagem permite que os serviços se comuniquem de forma eficiente, sem a necessidade de estarem acoplados de maneira síncrona.

### 4. **Estrutura de Mensageria:**
   Será configurado filas e exchanges no RabbitMQ para criar uma estrutura eficaz de mensageria. O `user-microservice` publicará mensagens indicando eventos relevantes, como o cadastro de um novo usuário, e o `email-microservice` estará inscrito para consumir essas mensagens e enviar e-mails de boas-vindas.

### 5. **Envio de E-mails com SMTP do Gmail:**
   O `email-microservice` integrará a API `JavaMailSender` para se conectar ao SMTP do Gmail, possibilitando o envio seguro e confiável de e-mails. Configurações adequadas serão aplicadas para garantir autenticação e criptografia na comunicação com o servidor SMTP.

# Microservice API User

## Arquivo de Configuração - `application.properties`

O arquivo `application.properties` configura o comportamento e os parâmetros de execução de um aplicativo Spring. No contexto do `user-microservice`, o arquivo de configuração fornece informações sobre o servidor, o banco de dados e a integração com serviços externos. 

```properties
server.port=8081

spring.datasource.url=jdbc:postgresql://localhost:5432/microservice-user
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update

# Configurações do RabbitMQ - Especifica os endereços do servidor RabbitMQ aos quais o aplicativo deve se conectar.
spring.rabbitmq.addresses=amqps://dhbykska:kPsWDJ2-NEvI1998Xe8jth4mjqJfWscz@shrimp.rmq.cloudamqp.com/dhbykska

# Configurando uma fila de mensagens especifica para o envio de correio eletronico. O "default.email" é o nome da fila de mensagens para o serviço de envio de e-mails.
broker.queue.email.name=default.email
```

### **1. Configuração do Servidor:**
```properties
server.port=8081
```
- **Descrição:** Define a porta em que o servidor incorporado do Spring Boot será iniciado. Neste caso, o serviço estará disponível na porta `8081`. A escolha de uma porta diferente da padrão (`8080`) pode ser útil para evitar conflitos com outros serviços em execução.

### **2. Configurações do Banco de Dados PostgreSQL:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/microservice-user
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
```
- **Descrição:** Essas configurações se referem à conexão com o banco de dados PostgreSQL.
  - `spring.datasource.url`: Especifica a URL de conexão com o banco de dados, indicando o tipo de banco de dados (`postgresql`), o host (`localhost`), a porta (`5432`), e o nome do banco de dados (`microservice-user`).
  - `spring.datasource.username` e `spring.datasource.password`: Credenciais de autenticação para o banco de dados PostgreSQL.
  - `spring.jpa.hibernate.ddl-auto`: Determina a estratégia de atualização do esquema do banco de dados. Neste caso, está configurado para `update`, o que significa que o Hibernate atualizará automaticamente o esquema do banco de dados com base nas entidades JPA.

### **3. Configurações do RabbitMQ:**
```properties
spring.rabbitmq.addresses=amqps://dhbykska:kPsWDJ2-NEvI1998Xe8jth4mjqJfWscz@shrimp.rmq.cloudamqp.com/dhbykska
```
- **Descrição:** Especifica os endereços do servidor RabbitMQ aos quais o aplicativo deve se conectar.
  - `spring.rabbitmq.addresses`: URL que contém as credenciais e informações de conexão para o RabbitMQ. Isso inclui o protocolo (`amqps`), nome de usuário, senha, endereço do servidor e vhost.

### **4. Configurações da Fila do RabbitMQ para Envio de E-mails:**
```properties
# Configurando uma fila de mensagens específica para o envio de correio eletrônico. 
# O "default.email" é o nome da fila de mensagens para o serviço de envio de e-mails.
broker.queue.email.name=default.email
```
- **Descrição:** Configura uma fila de mensagens específica para o serviço de envio de e-mails.
  - `broker.queue.email.name`: Nome da fila de mensagens. Neste caso, a fila é denominada `default.email`. As mensagens destinadas a esta fila serão consumidas pelo `email-microservice` para o envio de e-mails.

Essas configurações fornecem uma base sólida para a execução do `user-microservice`, incluindo a conexão com o banco de dados, a comunicação assíncrona com o RabbitMQ e a definição de parâmetros do servidor. A adaptação dessas configurações pode ser necessária dependendo do ambiente e dos requisitos específicos do sistema.

## Camada de Modelo - `UserModel`

A camada de modelo representa a estrutura de dados que será armazenada e manipulada pelo sistema. No caso específico, a classe `UserModel` encapsula as informações relacionadas aos usuários. 

```java
package com.microservice.user.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "TB_USERS")
public class UserModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID userId;

    private String name;
    private String email;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

### **1. Definição da Classe:**

A classe `UserModel` está contida no pacote `com.microservice.user.models`. Ela é anotada com `@Entity`, indicando que é uma entidade JPA (Java Persistence API) que será mapeada para uma tabela no banco de dados.

### **2. Mapeamento JPA:**

- **`@Table(name = "TB_USERS")`:** Essa anotação especifica o nome da tabela no banco de dados onde os objetos `UserModel` serão armazenados. Neste caso, os objetos serão armazenados na tabela denominada "TB_USERS".

- **`@Id` e `@GeneratedValue(strategy = GenerationType.AUTO)`:** Essas anotações são usadas para indicar que a propriedade `userId` é a chave primária da tabela. O valor da chave primária será gerado automaticamente (auto-incremento) pelo banco de dados, conforme especificado pela estratégia `GenerationType.AUTO`.

### **3. Propriedades:**

- **`private UUID userId`:** Representa o identificador único do usuário. O tipo `UUID` (Universally Unique Identifier) é utilizado para garantir a unicidade.

- **`private String name`:** Armazena o nome do usuário.

- **`private String email`:** Armazena o endereço de e-mail do usuário.

### **4. Métodos de Acesso (Getters e Setters):**

- Para cada propriedade, há métodos de acesso (`get` e `set`) que permitem obter e definir os valores das propriedades, seguindo as convenções JavaBeans.

### **5. Serialização:**

- **`implements Serializable`:** A implementação da interface `Serializable` sugere que os objetos dessa classe podem ser serializados, o que é importante para operações como persistência de dados ou transferência de objetos entre sistemas.

### **6. Identidade Única:**

- O uso de `UUID` para `userId` garante uma identidade única e independente da fonte, evitando colisões de identificadores.

### **7. Versão Serial:**

- **`private static final long serialVersionUID = 1L;`:** Fornece um número de versão para garantir a consistência durante a desserialização. Este é um requisito da interface `Serializable`.

- ## Camada de Repositório - `UserRepository`

A camada de repositório desempenha um papel crucial na interação com o banco de dados, permitindo operações de leitura e escrita relacionadas aos objetos da classe `UserModel`. No contexto do `user-microservice`, o `UserRepository` estende a interface `JpaRepository`, fornecendo métodos convenientes para realizar operações com o banco de dados. 

### **1. Definição do Repositório:**

A interface `UserRepository` está localizada no pacote `com.microservice.user.repositories` e define métodos que podem ser usados para interagir com o banco de dados para operações relacionadas aos usuários.

### **2. Herança de `JpaRepository`:**

```java
public interface UserRepository extends JpaRepository<UserModel, UUID> {
}
```

- **Descrição:** `UserRepository` estende a interface `JpaRepository`. Essa interface é fornecida pelo Spring Data JPA e oferece um conjunto de métodos padrão para operações de banco de dados relacionadas à entidade `UserModel`.

### **3. Tipos Genéricos da Interface `JpaRepository`:**

- **`UserModel`:** Indica a entidade JPA associada ao repositório. Neste caso, é a classe `UserModel` que representa a estrutura dos dados do usuário.

- **`UUID`:** Indica o tipo do identificador da entidade (`userId`). No caso, é um `UUID`, garantindo unicidade.

### **4. Métodos Herdados de `JpaRepository`:**

A interface `JpaRepository` fornece uma série de métodos herdados para realizar operações com o banco de dados sem a necessidade de escrever consultas SQL manualmente. Alguns exemplos comuns são:

- `save`: Salva uma entidade no banco de dados.
- `findById`: Recupera uma entidade por seu identificador.
- `findAll`: Recupera todas as entidades.
- `deleteById`: Exclui uma entidade por seu identificador.

## Camada de Serviço - `UserService`

A camada de serviço (`UserService`) desempenha um papel central no `user-microservice`, encapsulando a lógica de negócios e coordenando as operações entre a camada de controle (controlador) e a camada de persistência (repositório). 

```java
package com.microservice.user.services;

import com.microservice.user.models.UserModel;
import com.microservice.user.producers.UserProducer;
import com.microservice.user.repositories.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

   final UserRepository userRepository;

    final UserProducer userProducer;

   public UserService(UserRepository userRepository, UserProducer userProducer){
       this.userRepository = userRepository;
       this.userProducer = userProducer;
   }

   /**
    * A anotação @Transaction têm como objetivos a precaução para gerar o rollback entre as operações.
    * (1) Operação de salvar o user.
    * (2) Operação que irá publicar a mensagem que vai ser gerada pelo canal via Broker.
    * Ou seja, se uma das operações falhar é gerado o rollback*/
    @Transactional
    public UserModel save(UserModel userModel){
        userModel = userRepository.save(userModel);

        // vai chamar o método que encapsula a lógica de criação de uma mensagem de e-mail a partir de um objeto UserModel e a publicação dessa mensagem em uma fila RabbitMQ.
        userProducer.publishMessageEmail(userModel);

        return userModel;
    }
}
```

### **1. Definição da Classe:**

A classe `UserService` está localizada no pacote `com.microservice.user.services` e é anotada com `@Service`, indicando que é um componente de serviço gerenciado pelo Spring.

### **2. Dependências Injetadas:**

```java
public class UserService {

   final UserRepository userRepository;
   final UserProducer userProducer;

   public UserService(UserRepository userRepository, UserProducer userProducer){
       this.userRepository = userRepository;
       this.userProducer = userProducer;
   }
}
```

- **Descrição:** `UserService` possui duas dependências injetadas via construtor:
  - `UserRepository`: Responsável por interagir com o banco de dados e executar operações relacionadas ao `UserModel`.
  - `UserProducer`: Utilizado para a comunicação assíncrona com o `email-microservice` por meio de um sistema de mensageria (RabbitMQ).

### **3. Anotação `@Transactional`:**

```java
@Transactional
```

- **Descrição:** Essa anotação indica que todos os métodos públicos desta classe devem ser executados dentro de uma transação. Se uma exceção for lançada durante a execução de qualquer um desses métodos, a transação será revertida (rollback).

### **4. Método `save`:**

```java
public UserModel save(UserModel userModel){
    userModel = userRepository.save(userModel);
    userProducer.publishMessageEmail(userModel);
    return userModel;
}
```

- **Descrição:** Este método realiza a persistência de um novo usuário no banco de dados e, em seguida, aciona a publicação assíncrona de uma mensagem para o `email-microservice` através do `UserProducer`. A anotação `@Transactional` garante a consistência transacional entre essas operações.

- **Lógica Detalhada:**
  1. **Salvar o Usuário no Banco de Dados (`userRepository.save(userModel)`):** Persiste o objeto `userModel` no banco de dados. Se a operação falhar, a transação será revertida.
  2. **Publicar Mensagem para o `email-microservice` (`userProducer.publishMessageEmail(userModel)`):** Chama o método responsável por encapsular a lógica de criação de uma mensagem de e-mail a partir do objeto `UserModel` e publicar essa mensagem em uma fila RabbitMQ. Esta operação também está dentro do contexto transacional.

### **5. Utilização de Serviço Assíncrono:**

A publicação assíncrona da mensagem para o `email-microservice` permite que a execução do serviço `UserService` não seja bloqueada pela conclusão do serviço de envio de e-mails.

# Camada DTOs - `UserRecordDto`

A camada de DTOs (Data Transfer Objects) desempenha a transferência de dados entre as camadas do aplicativo. No contexto do `user-microservice`, a classe `UserRecordDto` é um exemplo de um DTO implementado como um `record` em Java, introduzido no Java 14. 

```java
package com.microservice.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record UserRecordDto(@NotBlank String name, @NotBlank @Email String email) {
}
```

### **1. Definição da Classe:**

A classe `UserRecordDto` está localizada no pacote `com.microservice.user.dtos` e é um exemplo de DTO usado para transferir dados relacionados a usuários entre diferentes partes do sistema.

### **2. Anotações de Validação:**

```java
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
```

- **Descrição:** As anotações `@NotBlank` e `@Email` são provenientes do pacote `jakarta.validation.constraints` e são utilizadas para impor regras de validação nos campos da classe.
  - `@NotBlank`: Garante que a string não seja nula e tenha pelo menos um caractere não branco.
  - `@Email`: Assegura que a string represente um endereço de e-mail válido.

### **3. Estrutura como `record`:**

```java
public record UserRecordDto(@NotBlank String name, @NotBlank @Email String email) {
}
```

- **Descrição:** A classe `UserRecordDto` é definida como um `record`, uma feature introduzida no Java 14. Um `record` é uma forma concisa de criar classes imutáveis, principalmente utilizadas para representar dados. O próprio Java gera automaticamente métodos como `equals()`, `hashCode()`, e `toString()` com base nos campos declarados.

### **4. Campos da Classe:**

- **`name`:**
  - Tipo: `String`
  - Descrição: Representa o nome do usuário. A anotação `@NotBlank` garante que não seja nulo e tenha pelo menos um caractere não branco.

- **`email`:**
  - Tipo: `String`
  - Descrição: Representa o endereço de e-mail do usuário. A anotação `@NotBlank` garante que não seja nulo e tenha pelo menos um caractere não branco, enquanto `@Email` valida se a string representa um endereço de e-mail válido.

### **5. Uso de `record` para Imutabilidade:**

O uso de `record` proporciona imutabilidade aos objetos dessa classe, tornando-os seguros para compartilhamento entre diferentes partes do sistema sem riscos de alterações inadvertidas.

### **6. Facilidade de Leitura:**

O uso de `record` reduz significativamente a quantidade de código necessário para criar classes simples de transferência de dados, tornando o código mais limpo e fácil de ler.

# Camada DTOs - `EmailDto`

A classe `EmailDto` na camada de DTOs é responsável por estruturar dados relacionados ao envio de e-mails, proporcionando uma forma organizada de transferir informações relevantes entre diferentes partes do sistema.

```java
package com.microservice.user.dtos;

import java.util.UUID;

public class EmailDto {

    private UUID userId;
    private String emailTo;
    private String subject;
    private String text;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
```

### **1. Definição da Classe:**

A classe `EmailDto` está localizada no pacote `com.microservice.user.dtos` e é utilizada para representar os dados necessários para o envio de e-mails.

### **2. Campos da Classe:**

```java
public class EmailDto {
    private UUID userId;
    private String emailTo;
    private String subject;
    private String text;
}
```

- **Descrição:**
  - `userId`: Representa o identificador único associado ao usuário para o qual o e-mail será enviado.
  - `emailTo`: Armazena o endereço de e-mail do destinatário.
  - `subject`: Representa o assunto do e-mail.
  - `text`: Armazena o corpo do e-mail.

### **3. Métodos de Acesso (Getters e Setters):**

```java
public UUID getUserId() {
    return userId;
}

public void setUserId(UUID userId) {
    this.userId = userId;
}

public String getEmailTo() {
    return emailTo;
}

public void setEmailTo(String emailTo) {
    this.emailTo = emailTo;
}

public String getSubject() {
    return subject;
}

public void setSubject(String subject) {
    this.subject = subject;
}

public String getText() {
    return text;
}

public void setText(String text) {
    this.text = text;
}
```

- **Descrição:** Métodos de acesso (getters e setters) são fornecidos para cada campo da classe, seguindo as convenções JavaBeans. Esses métodos permitem a obtenção e definição dos valores dos campos.

### **4. Utilidade da Classe:**

A classe `EmailDto` é fundamental para a transferência de informações entre o `user-microservice` e o `email-microservice`, permitindo a comunicação eficiente sobre o envio de e-mails sem a necessidade de exposição direta dos detalhes de implementação.

# Camada de Controller - `UserController`

A camada de controller é responsável por receber requisições HTTP, interpretar essas requisições e coordenar as chamadas para a camada de serviço correspondente. No contexto do `user-microservice`, a classe `UserController` gerencia as operações relacionadas aos usuários. 

```java
package com.microservice.user.controllers;

import com.microservice.user.dtos.UserRecordDto;
import com.microservice.user.models.UserModel;
import com.microservice.user.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // http://localhost:8081/users/create
    @PostMapping("/create")
    public ResponseEntity<UserModel> saveUser(@RequestBody @Valid UserRecordDto userRecordDto){

        var userModel = new UserModel();

        BeanUtils.copyProperties(userRecordDto, userModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(userModel));
    }

}
```

### **1. Definição da Classe:**

A classe `UserController` está localizada no pacote `com.microservice.user.controllers` e é um componente do Spring, anotado com `@RestController`, indicando que responde a requisições HTTP e que os resultados dessas requisições são retornados diretamente como respostas HTTP no formato JSON.

### **2. Dependência Injetada:**

```java
public class UserController {

    final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
}
```

- **Descrição:** `UserController` possui uma dependência injetada via construtor:
  - `UserService`: A camada de serviço responsável pela lógica de negócios relacionada aos usuários.

### **3. Mapeamento de Requisições:**

```java
@RestController
@RequestMapping("/users")
```

- **Descrição:** As anotações `@RestController` e `@RequestMapping` são usadas para definir a base da URL para todas as requisições manipuladas pela classe. Neste caso, todas as requisições começarão com "/users".

### **4. Método `saveUser`:**

```java
@PostMapping("/create")
public ResponseEntity<UserModel> saveUser(@RequestBody @Valid UserRecordDto userRecordDto){

    var userModel = new UserModel();

    BeanUtils.copyProperties(userRecordDto, userModel);

    return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(userModel));
}
```

- **Descrição:** Este método é mapeado para a URL "/users/create" e lida com requisições do tipo `POST`. Ele é responsável por receber dados de um novo usuário, convertê-los para o formato adequado (`UserModel`), e então chamar o serviço para persistir o usuário no banco de dados.

- **Detalhes do Método:**
  - `@PostMapping("/create")`: Mapeia a URL "/users/create" para requisições do tipo `POST`.
  - `@RequestBody @Valid UserRecordDto userRecordDto`: Anotação que indica que o corpo da requisição contém dados que devem ser convertidos para um objeto `UserRecordDto`. A anotação `@Valid` indica que as validações declaradas na classe `UserRecordDto` devem ser aplicadas.
  - `var userModel = new UserModel();`: Cria uma instância de `UserModel` para armazenar os dados do novo usuário.
  - `BeanUtils.copyProperties(userRecordDto, userModel);`: Copia as propriedades do `UserRecordDto` para o `UserModel`.
  - `return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(userModel));`: Retorna uma resposta HTTP com o status "201 Created" e o objeto `UserModel` criado pelo serviço após a persistência no banco de dados.

# Camada de Configuração - `RabbitMQConfig`

A camada de configuração (`RabbitMQConfig`) é responsável por definir e configurar componentes específicos da aplicação. No contexto do `user-microservice`, a classe `RabbitMQConfig` concentra-se na configuração do conversor de mensagens para a comunicação com o RabbitMQ. 

```java
package com.microservice.user.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * Esse método será usada para converter objetos Java em mensagens JSON ao enviar mensagens para RabbitMQ e vice-versa ao receber mensagens da fila.*/
    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        ObjectMapper objectMapper = new ObjectMapper();

        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
```

### **1. Definição da Classe:**

A classe `RabbitMQConfig` está localizada no pacote `com.microservice.user.configs` e é anotada com `@Configuration`, indicando que fornece configurações específicas para a aplicação.

### **2. Método `messageConverter`:**

```java
@Configuration
public class RabbitMQConfig {

    /**
     * Esse método será usada para converter objetos Java em mensagens JSON ao enviar mensagens para RabbitMQ e vice-versa ao receber mensagens da fila.*/
    @Bean
    public Jackson2JsonMessageConverter messageConverter(){
        ObjectMapper objectMapper = new ObjectMapper();

        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
```

- **Descrição:** Este método é anotado com `@Bean` e é responsável por fornecer um `Jackson2JsonMessageConverter`, que é um conversor de mensagens usado para serializar e desserializar objetos Java em formato JSON. Isso é essencial para a comunicação eficiente com o RabbitMQ, que utiliza JSON para a representação das mensagens.

- **Detalhes do Método:**
  - `ObjectMapper objectMapper = new ObjectMapper();`: Cria uma instância de `ObjectMapper`, uma classe da biblioteca Jackson que lida com a conversão entre objetos Java e JSON.
  - `return new Jackson2JsonMessageConverter(objectMapper);`: Retorna um novo `Jackson2JsonMessageConverter`, configurado com o `ObjectMapper` criado. Este conversor é utilizado pelo Spring para realizar a conversão de objetos Java em mensagens JSON ao enviar mensagens para o RabbitMQ e vice-versa ao receber mensagens da fila.

### **3. Propósito do Método:**

- **Conversão de Objetos Java para JSON e vice-versa:**
  - Este método desempenha um papel crucial na comunicação assíncrona com o RabbitMQ. Ele assegura que os objetos Java enviados para a fila como mensagens sejam convertidos para o formato JSON apropriado, permitindo a transmissão eficiente e a desserialização correta quando as mensagens são recebidas da fila.

### **4. Configuração Personalizada:**

- **Configuração de Conversão de Mensagens:**
  - A configuração personalizada deste conversor de mensagens permite ajustar o comportamento de serialização e desserialização de objetos Java para atender aos requisitos específicos da aplicação.

### **5. Conclusão:**

A classe `RabbitMQConfig` fornece uma configuração essencial para a comunicação assíncrona do `user-microservice` com o RabbitMQ. Ao definir o conversor de mensagens, ela assegura uma transição eficiente de dados entre objetos Java e o formato JSON exigido pelo RabbitMQ, contribuindo para a integração adequada entre os microserviços.

# Camada de Produtores - `UserProducer`

A camada de produtores (`UserProducer`) é responsável por facilitar a comunicação assíncrona entre o `user-microservice` e o `email-microservice` por meio do RabbitMQ. 

```java
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
```

### **1. Definição da Classe:**

A classe `UserProducer` está localizada no pacote `com.microservice.user.producers` e é anotada com `@Component`, indicando que é um componente gerenciado pelo Spring.

### **2. Dependência Injetada:**

```java
public class UserProducer {

    final RabbitTemplate rabbitTemplate;

    public UserProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }
}
```

- **Descrição:** `UserProducer` possui uma dependência injetada via construtor:
  - `RabbitTemplate`: Uma classe fornecida pelo Spring para facilitar o envio de mensagens para filas ou exchanges no RabbitMQ.

### **3. Variável de Configuração - `routingKey`:**

```java
@Value(value = "${broker.queue.email.name}")
private String routingKey;
```

- **Descrição:** Essa variável é anotada com `@Value` e é injetada a partir do arquivo de propriedades (`application.properties`). Ela representa a chave de roteamento que será utilizada ao publicar mensagens na fila RabbitMQ.

### **4. Método `publishMessageEmail`:**

```java
public void publishMessageEmail(UserModel userModel){
    var emailDto = new EmailDto();

    emailDto.setUserId(userModel.getUserId());
    emailDto.setEmailTo(userModel.getEmail());
    emailDto.setSubject("Cadastro realizado com sucesso!");
    emailDto.setText(userModel.getName() + ", seja bem vindo(a)! Agradecemos o seu cadastro.");

    rabbitTemplate.convertAndSend("", routingKey, emailDto);
}
```

- **Descrição:** Este método é responsável por criar uma instância de `EmailDto` com os dados do usuário, configurar a mensagem de e-mail e publicar essa mensagem na fila RabbitMQ associada à troca (exchange).

- **Detalhes do Método:**
  - `var emailDto = new EmailDto();`: Cria uma instância de `EmailDto` para representar os dados da mensagem de e-mail.
  - `emailDto.setUserId(userModel.getUserId());`: Configura o identificador único do usuário no `EmailDto`.
  - `emailDto.setEmailTo(userModel.getEmail());`: Configura o destinatário do e-mail no `EmailDto`.
  - `emailDto.setSubject("Cadastro realizado com sucesso!");`: Configura o assunto do e-mail no `EmailDto`.
  - `emailDto.setText(userModel.getName() + ", seja bem vindo(a)! Agradecemos o seu cadastro.");`: Configura o texto do e-mail no `EmailDto`.
  - `rabbitTemplate.convertAndSend("", routingKey, emailDto);`: Utiliza o `RabbitTemplate` para converter e enviar a mensagem para a fila RabbitMQ associada à troca (exchange). O primeiro parâmetro vazio indica que será utilizada a exchange default.

### **5. Integração Assíncrona:**

- **Objetivo da Classe:**
  - `UserProducer` facilita a integração assíncrona entre o `user-microservice` e o `email-microservice` ao publicar mensagens na fila RabbitMQ.

### **6. Conclusão:**

A classe `UserProducer` é um componente para a arquitetura de microserviços, permitindo a comunicação eficiente e assíncrona entre serviços distintos. Ao utilizar o RabbitMQ como intermediário, ela contribui para a escalabilidade e resiliência do sistema, permitindo que os microserviços operem de forma independente e respondam a eventos assíncronos.

# Feito por: `Daniel Penelva de Andrade`
