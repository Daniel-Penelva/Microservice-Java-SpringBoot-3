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

# Microservice API Email

# Arquivo de Propriedades - `application.properties`

O arquivo de propriedades (`application.properties`) é utilizado para configurar e personalizar diversos aspectos da aplicação. No contexto do `email-microservice`, este arquivo desempenha um papel fundamental na configuração de propriedades relacionadas ao servidor, banco de dados, RabbitMQ, e-mail e outros. 

### **1. Configurações do Servidor:**

```properties
server.port=8082
```

- **Descrição:** Define a porta na qual o servidor do `email-microservice` será executado. Neste caso, o serviço estará acessível na porta `8082`.

### **2. Configurações do Banco de Dados:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/microservice-correio-eletronico
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
```

- **Descrição:**
  - `spring.datasource.url`: Define a URL do banco de dados PostgreSQL.
  - `spring.datasource.username`: Define o nome de usuário do banco de dados.
  - `spring.datasource.password`: Define a senha do banco de dados.
  - `spring.jpa.hibernate.ddl-auto`: Define a estratégia de atualização do esquema do banco de dados. Neste caso, a opção `update` permite que o Hibernate crie automaticamente as tabelas se não existirem.

### **3. Configurações do RabbitMQ:**

```properties
spring.rabbitmq.addresses=amqps://dhbykska:kPsWDJ2-NEvI1998Xe8jth4mjqJfWscz@shrimp.rmq.cloudamqp.com/dhbykska
broker.queue.email.name=default.email
```

- **Descrição:**
  - `spring.rabbitmq.addresses`: Especifica os endereços do servidor RabbitMQ aos quais o aplicativo deve se conectar.
  - `broker.queue.email.name`: Configura o nome da fila de mensagens para o serviço de envio de e-mails.

### **4. Configurações de Envio de E-mails:**

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=testeaplicacao14@gmail.com
spring.mail.password=lhsc yrib wqfx aqni
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

- **Descrição:**
  - `spring.mail.host`: Define o host do servidor SMTP para envio de e-mails (no exemplo, é utilizado o SMTP do Gmail).
  - `spring.mail.port`: Define a porta do servidor SMTP.
  - `spring.mail.username`: Define o nome de usuário para autenticação no servidor SMTP.
  - `spring.mail.password`: Define a senha para autenticação no servidor SMTP.
  - `spring.mail.properties.mail.smtp.auth`: Habilita a autenticação SMTP.
  - `spring.mail.properties.mail.smtp.starttls.enable`: Habilita o STARTTLS, uma extensão de segurança para o protocolo de transferência de correio simples (SMTP).

### **5. Conclusão:**

O arquivo de propriedades (`application.properties`) vai configurar o comportamento do `email-microservice`. Ao definir parâmetros como porta do servidor, conexão com banco de dados, configurações do RabbitMQ e detalhes de envio de e-mails, ele permite que o microserviço opere de acordo com os requisitos específicos da aplicação. Essas configurações são essenciais para garantir a comunicação adequada, persistência de dados e funcionalidade de envio de e-mails.

# Camada Model - `EmailModel`

A camada model (`EmailModel`) é responsável por representar a estrutura de dados dos e-mails no contexto do `email-microservice`.

```java
package com.microservice.email.models;

import com.microservice.email.enums.StatusEmail;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "TB_EMAILS")
public class EmailModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID emailId;

    private UUID userId;
    private String emailFrom; /* quem envia o email */
    private String emailTo; /* para quem envia o email */
    private String subject; /* título do email */

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDateTime sendDateEmail;
    private StatusEmail statusEmail;

    public UUID getEmailId() {
        return emailId;
    }

    public void setEmailId(UUID emailId) {
        this.emailId = emailId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
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

    public LocalDateTime getSendDateEmail() {
        return sendDateEmail;
    }

    public void setSendDateEmail(LocalDateTime sendDateEmail) {
        this.sendDateEmail = sendDateEmail;
    }

    public StatusEmail getStatusEmail() {
        return statusEmail;
    }

    public void setStatusEmail(StatusEmail statusEmail) {
        this.statusEmail = statusEmail;
    }
}
```

### **1. Definição da Classe:**

A classe `EmailModel` está localizada no pacote `com.microservice.email.models` e é anotada com `@Entity`, indicando que ela é uma entidade persistente e pode ser mapeada para uma tabela em um banco de dados relacional.

### **2. Campos da Classe:**

```java
public class EmailModel implements Serializable {

    // ... (códigos anteriores)

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID emailId;

    private UUID userId;
    private String emailFrom;
    private String emailTo;
    private String subject;
    private String text;

    @Column(columnDefinition = "TEXT")
    private LocalDateTime sendDateEmail;
    private StatusEmail statusEmail;
}
```

- **Descrição:**
  - `emailId`: Identificador único associado a cada e-mail (gerado automaticamente).
  - `userId`: Identificador único do usuário associado ao e-mail.
  - `emailFrom`: Endereço de e-mail do remetente.
  - `emailTo`: Endereço de e-mail do destinatário.
  - `subject`: Título do e-mail.
  - `text`: Corpo do e-mail.
  - `sendDateEmail`: Data e hora do envio do e-mail.
  - `statusEmail`: Status do e-mail, representado por um enum `StatusEmail` (definido em `com.microservice.email.enums`).

### **3. Métodos de Acesso (Getters e Setters):**

```java
public UUID getEmailId() { /*...*/ }
public void setEmailId(UUID emailId) { /*...*/ }

public UUID getUserId() { /*...*/ }
public void setUserId(UUID userId) { /*...*/ }

public String getEmailFrom() { /*...*/ }
public void setEmailFrom(String emailFrom) { /*...*/ }

public String getEmailTo() { /*...*/ }
public void setEmailTo(String emailTo) { /*...*/ }

public String getSubject() { /*...*/ }
public void setSubject(String subject) { /*...*/ }

public String getText() { /*...*/ }
public void setText(String text) { /*...*/ }

public LocalDateTime getSendDateEmail() { /*...*/ }
public void setSendDateEmail(LocalDateTime sendDateEmail) { /*...*/ }

public StatusEmail getStatusEmail() { /*...*/ }
public void setStatusEmail(StatusEmail statusEmail) { /*...*/ }
```

- **Descrição:** Métodos de acesso (getters e setters) são fornecidos para cada campo da classe, seguindo as convenções JavaBeans. Esses métodos permitem a obtenção e definição dos valores dos campos.

### **4. Anotações Adicionais:**

```java
@Column(columnDefinition = "TEXT")
```

- **Descrição:** Essa anotação é usada para definir a coluna `text` como um tipo `TEXT` no banco de dados, permitindo armazenar conteúdos mais longos, como o corpo do e-mail.

# Camada de Modelo - Enum `StatusEmail`

O enum `StatusEmail` está localizado no pacote `com.microservice.email.enums` e é utilizado na camada de modelo para representar o status de um e-mail no contexto do `email-microservice`. Abaixo, fornecemos uma documentação detalhada para este enum.

### **1. Definição do Enum:**

O enum `StatusEmail` é uma enumeração que representa os possíveis estados de um e-mail.

```java
public enum StatusEmail {
    SENT, ERROR;
}
```

### **2. Valores do Enum:**

- `SENT`: Indica que o e-mail foi enviado com sucesso.
- `ERROR`: Indica que ocorreu um erro durante o envio do e-mail.

### **3. Utilização:**

O enum `StatusEmail` é utilizado na classe `EmailModel` como um campo para representar o status de um e-mail.

```java
public class EmailModel implements Serializable {
    // ... (códigos anteriores)
    private StatusEmail statusEmail;
    // ... (códigos posteriores)
}
```

### **4. Significado dos Estados:**

- **`SENT` (Enviado):**
  - Indica que o e-mail foi enviado com sucesso sem problemas.

- **`ERROR` (Erro):**
  - Indica que ocorreu um erro durante o envio do e-mail. Essa categoria pode incluir falhas como problemas na conexão com o servidor de e-mails, endereço de destino inválido, entre outros.

### **5. Uso no `EmailService`:**

O enum é utilizado no método `sendEmail` da classe `EmailService` para atribuir o status do e-mail com base no resultado do envio.

```java
public class EmailService {
    // ... (códigos anteriores)
    public EmailModel sendEmail(EmailModel emailModel){
        try{
            // ... (códigos anteriores)

            emailModel.setStatusEmail(StatusEmail.SENT);

        }catch (MailException e){
            emailModel.setStatusEmail(StatusEmail.ERROR);
        }finally {
            return  emailRepository.save(emailModel);
        }
    }
    // ... (códigos posteriores)
}
```

### **6. Conclusão:**

O enum `StatusEmail` proporciona uma maneira clara e legível de representar os diferentes estados possíveis de um e-mail no `email-microservice`. Ao utilizar esse enum como um campo na classe `EmailModel`, a aplicação pode facilmente distinguir entre e-mails enviados com sucesso e e-mails que encontraram algum tipo de erro durante o processo de envio. Isso simplifica a lógica de manipulação de estados e fornece uma abordagem estruturada para lidar com resultados de envio de e-mails.

# Camada Repository - `EmailRepository`

A camada repository (`EmailRepository`) é responsável por fornecer métodos para acessar e manipular dados da entidade `EmailModel` no banco de dados. 

```java
package com.microservice.email.repositories;

import com.microservice.email.models.EmailModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailRepository extends JpaRepository<EmailModel, UUID> {
}
```

### **1. Definição da Interface:**

A interface `EmailRepository` está localizada no pacote `com.microservice.email.repositories` e estende a interface `JpaRepository` fornecida pelo Spring Data JPA.

```java
public interface EmailRepository extends JpaRepository<EmailModel, UUID> {
}
```

- **Descrição:**
  - `JpaRepository<EmailModel, UUID>`: `EmailRepository` estende `JpaRepository`, indicando que herda métodos de acesso e manipulação de dados padrão para a entidade `EmailModel`. O segundo parâmetro `UUID` especifica o tipo do identificador único da entidade.

### **2. Métodos Herdados:**

A interface `JpaRepository` fornece uma variedade de métodos herdados para realizar operações comuns em uma entidade, como salvar, buscar, atualizar e excluir registros no banco de dados.

- Exemplos de métodos herdados incluem:
  - `save(T entity)`: Salva uma entidade no banco de dados.
  - `findById(ID id)`: Busca uma entidade por seu identificador único.
  - `findAll()`: Retorna todas as entidades no banco de dados.
  - `deleteById(ID id)`: Exclui uma entidade com base em seu identificador único.

# Camada Service - `EmailService`

A camada service (`EmailService`) é responsável por fornecer serviços relacionados ao envio de e-mails no contexto do `email-microservice`. 

```java
package com.microservice.email.services;

import com.microservice.email.enums.StatusEmail;
import com.microservice.email.models.EmailModel;
import com.microservice.email.repositories.EmailRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class EmailService {

    final EmailRepository emailRepository;
    final JavaMailSender emailSender; /** Interface JavaMailSender é utilizada para envio e recebimento de emails usando a API JavaMail padrão */

    public EmailService(EmailRepository emailRepository, JavaMailSender emailSender) {
        this.emailRepository = emailRepository;
        this.emailSender = emailSender;
    }

    @Value(value = "${spring.mail.username}")
    private String emailFrom;

    /** Método utilizado com implementar o corpo do email e enviá-lo */
    @Transactional
    public EmailModel sendEmail(EmailModel emailModel){
        try{
            emailModel.setSendDateEmail(LocalDateTime.now());
            emailModel.setEmailFrom(emailFrom);

            /** A classe SimpleMailMessage fornece uma representação simples de um e-mail.*/
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(emailModel.getEmailTo());
            message.setSubject(emailModel.getSubject());
            message.setText(emailModel.getText());

            emailSender.send(message);

            // Envia um Status de enviado (declarado no Enum StatusEmail)
            emailModel.setStatusEmail(StatusEmail.SENT);

        }catch (MailException e){
            emailModel.setStatusEmail(StatusEmail.ERROR);
        }finally {
            return  emailRepository.save(emailModel);
        }
    }

}
```

### **1. Definição da Classe:**

A classe `EmailService` está localizada no pacote `com.microservice.email.services` e é anotada com `@Service`, indicando que é um componente de serviço gerenciado pelo Spring.

```java
@Service
public class EmailService {
    // ... (códigos anteriores)
}
```

### **2. Dependências Injetadas:**

A classe `EmailService` possui duas dependências injetadas via construtor:

```java
public EmailService(EmailRepository emailRepository, JavaMailSender emailSender) {
    this.emailRepository = emailRepository;
    this.emailSender = emailSender;
}
```

- **Descrição:**
  - `emailRepository`: Instância de `EmailRepository` que fornece métodos para acessar e manipular dados relacionados a e-mails no banco de dados.
  - `emailSender`: Instância de `JavaMailSender`, uma interface que facilita o envio e recebimento de e-mails usando a API JavaMail padrão.

### **3. Configuração do Endereço de E-mail do Remetente:**

```java
@Value(value = "${spring.mail.username}")
private String emailFrom;
```

- **Descrição:**
  - A anotação `@Value` é usada para injetar o valor da propriedade `spring.mail.username` no campo `emailFrom`. Essa propriedade geralmente contém o endereço de e-mail do remetente.

### **4. Método `sendEmail`:**

```java
@Transactional
public EmailModel sendEmail(EmailModel emailModel){
    try{
        // ... (códigos anteriores)

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailModel.getEmailTo());
        message.setSubject(emailModel.getSubject());
        message.setText(emailModel.getText());

        emailSender.send(message);

        // Envia um Status de enviado (declarado no Enum StatusEmail)
        emailModel.setStatusEmail(StatusEmail.SENT);

    }catch (MailException e){
        emailModel.setStatusEmail(StatusEmail.ERROR);
    }finally {
        return  emailRepository.save(emailModel);
    }
}
```

- **Descrição:**
  - O método `sendEmail` é responsável por preparar, enviar e salvar informações sobre um e-mail.
  - `@Transactional`: Garante que a transação é gerenciada pelo Spring, permitindo o rollback se ocorrerem exceções.
  - `emailModel.setSendDateEmail(LocalDateTime.now())`: Define a data e hora do envio do e-mail como o momento atual.
  - `emailModel.setEmailFrom(emailFrom)`: Define o endereço de e-mail do remetente.
  - `SimpleMailMessage message = new SimpleMailMessage();`: Cria uma instância de `SimpleMailMessage` para representar a mensagem de e-mail.
  - `emailSender.send(message);`: Utiliza `emailSender` para enviar a mensagem de e-mail.
  - `emailModel.setStatusEmail(StatusEmail.SENT)`: Define o status do e-mail como "enviado" no caso de sucesso.
  - `catch (MailException e)`: Captura exceções relacionadas ao envio de e-mail, definindo o status do e-mail como "erro".
  - `finally`: Independentemente do resultado, salva as informações do e-mail no banco de dados usando `emailRepository.save(emailModel)`.

# Camada DTOs - Record `EmailRecordDto`

A classe `EmailRecordDto` está localizada no pacote `com.microservice.email.dtos` e é um record que representa um objeto de transferência de dados (DTO) no contexto do `email-microservice`. 

```java
package com.microservice.email.dtos;

import java.util.UUID;

public record EmailRecordDto(UUID userId, String emailTo, String subject, String text) {
}
```

### **1. Definição do Record:**

O record `EmailRecordDto` é uma estrutura de dados imutável e concisa, introduzida no Java 14, que é usada para representar dados de e-mails antes de serem processados pelo `email-microservice`.

```java
public record EmailRecordDto(UUID userId, String emailTo, String subject, String text) {
}
```

### **2. Campos do Record:**

- `userId`: Identificador único do usuário associado ao e-mail.
- `emailTo`: Endereço de e-mail do destinatário.
- `subject`: Título do e-mail.
- `text`: Corpo do e-mail.

### **3. Propriedades do Record:**

- **Imutabilidade:**
  - Como um record, os campos são automaticamente finais e imutáveis, o que significa que uma vez atribuído um valor, ele não pode ser alterado.

- **Métodos Gerados Automaticamente:**
  - O record gera automaticamente métodos `equals`, `hashCode`, e `toString`, simplificando a implementação desses métodos.

### **5. Vantagens do Record:**

- **Sintaxe Concisa:**
  - A sintaxe concisa do record torna a definição e o uso de DTOs mais compactos e legíveis.

- **Imutabilidade Automática:**
  - A imutabilidade automática simplifica o gerenciamento de estado e contribui para a robustez do código.

# Camada de Configuração - `RabbitMQConfig`

A classe `RabbitMQConfig` está localizada no pacote `com.microservice.email.configs` e é responsável por configurar aspectos relacionados ao RabbitMQ, um sistema de mensagens para o `email-microservice`. 

```java
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
```

### **1. Anotação `@Configuration`:**

```java
@Configuration
public class RabbitMQConfig {
    // ... (códigos anteriores)
}
```

- **Descrição:**
  - A anotação `@Configuration` indica que a classe é uma classe de configuração Spring, fornecendo configurações para o contexto de aplicação.

### **2. Dependências Injetadas:**

```java
@Value("${broker.queue.email.name}")
private String queue;
```

- **Descrição:**
  - `@Value("${broker.queue.email.name}")`: Injeta o valor da propriedade `broker.queue.email.name` no campo `queue`. Esta propriedade representa o nome da fila RabbitMQ configurada.

### **3. Método `queue`:**

```java
@Bean
public Queue queue(){
    return new Queue(queue, true);
}
```

- **Descrição:**
  - O método `queue` é anotado com `@Bean`, indicando que é um bean gerenciado pelo Spring.
  - Cria e retorna uma instância de `Queue` (fila) com o nome especificado pela propriedade `broker.queue.email.name`. O segundo parâmetro (`true`) indica que a fila é durável, ou seja, persistirá mesmo após o reinício do servidor RabbitMQ.

### **4. Método `messageConverter`:**

```java
@Bean
public Jackson2JsonMessageConverter messageConverter(){
    ObjectMapper objectMapper = new ObjectMapper();
    return new Jackson2JsonMessageConverter(objectMapper);
}
```

- **Descrição:**
  - O método `messageConverter` é anotado com `@Bean`, indicando que é um bean gerenciado pelo Spring.
  - Cria e retorna uma instância de `Jackson2JsonMessageConverter`, que será usada para converter objetos Java em mensagens JSON ao enviar mensagens para RabbitMQ e vice-versa ao receber mensagens da fila.
 
## Sobre a classe `Jackson2JsonMessageConverter`

A classe `Jackson2JsonMessageConverter` faz parte do ecossistema Spring para mensageria e é utilizada para converter objetos Java em mensagens JSON ao enviar mensagens para o RabbitMQ e vice-versa ao receber mensagens da fila. Ela é especialmente útil quando você precisa serializar e desserializar objetos complexos para mensagens JSON em um ambiente de comunicação assíncrona, como o RabbitMQ.

APontos-chave sobre a classe `Jackson2JsonMessageConverter`:

1. **Objetivo:**
   - O principal objetivo desta classe é facilitar a conversão entre objetos Java e representações JSON durante a troca de mensagens em um sistema baseado em mensageria.

2. **Integração com Jackson:**
   - Ela utiliza a biblioteca Jackson, que é uma biblioteca de serialização/desserialização JSON muito popular no ecossistema Java.

3. **Configuração:**
   - Normalmente, é configurada como um bean no contexto da aplicação Spring. 

4. **Conversão para JSON:**
   - Ao enviar mensagens para o RabbitMQ, esta classe converte objetos Java em representações JSON que são então enviadas como payload da mensagem.

5. **Conversão de JSON para Objeto Java:**
   - Ao receber mensagens da fila RabbitMQ, esta classe faz a conversão inversa, transformando o payload JSON de uma mensagem de volta para um objeto Java.

## Sobre a classe `ObjectMapper`

A classe `ObjectMapper` faz parte da biblioteca Jackson, uma das bibliotecas mais populares para manipulação de JSON em Java. O `ObjectMapper` é responsável por mapear objetos Java para JSON (serialização) e JSON para objetos Java (desserialização).

## Sobre `RabbitMQConfig`

A classe `RabbitMQConfig` desempenha um papel essencial na configuração do RabbitMQ para o `email-microservice`. Ao definir a fila RabbitMQ e o conversor de mensagens JSON, esta classe garante que a comunicação assíncrona entre microservices seja eficiente e semântica. Além disso, ao usar anotações Spring como `@Configuration` e `@Bean`, a classe se integra perfeitamente ao ciclo de vida de inicialização do Spring, proporcionando uma configuração centralizada e fácil de entender.

Obrigado por fornecer o contexto. O site [CloudAMQP](https://www.cloudamqp.com/) é uma plataforma que oferece serviços gerenciados para RabbitMQ na nuvem. Ele facilita a implantação e o gerenciamento de instâncias RabbitMQ sem a necessidade de configurar e manter um servidor RabbitMQ por conta própria.

Através do CloudAMQP, você pode provisionar e escalar instâncias do RabbitMQ, monitorar o desempenho, configurar filas, exchanges e outros recursos relacionados à mensageria.

Para obter informações detalhadas sobre os microservices da fila RabbitMQ hospedados no CloudAMQP, geralmente você pode usar a interface web fornecida pela própria plataforma. Essa interface web pode oferecer métricas, estatísticas e outras informações relacionadas à operação e desempenho dos seus serviços de mensageria.

Ao acessar o CloudAMQP, você deve encontrar ferramentas e recursos para gerenciar suas instâncias RabbitMQ, monitorar o tráfego, configurar filas e exchanges, entre outras funcionalidades relacionadas à RabbitMQ na nuvem. 

# Camada de Consumers - `EmailConsumer`

A classe `EmailConsumer` está localizada no pacote `com.microservice.email.consumers` e desempenha um papel fundamental no processo de consumir mensagens da fila RabbitMQ e encaminhá-las para processamento adicional. 

### **1. Anotação `@Component`:**

```java
@Component
public class EmailConsumer {
    // ... (códigos anteriores)
}
```

- **Descrição:**
  - A anotação `@Component` indica que a classe é um componente gerenciado pelo Spring, permitindo que seja automaticamente detectada e configurada durante o escaneamento de componentes.

### **2. Dependências Injetadas:**

```java
final EmailService emailService;
```

- **Descrição:**
  - O construtor da classe injeta uma instância de `EmailService`, permitindo que o `EmailConsumer` utilize os serviços fornecidos pela camada de serviço (`EmailService`).

### **3. Método `listenEmailQueue`:**

```java
@RabbitListener(queues = "${broker.queue.email.name}")
public void listenEmailQueue(@Payload EmailRecordDto emailRecordDto){
    // ... (códigos dentro do método)
}
```

- **Descrição:**
  - O método `listenEmailQueue` é anotado com `@RabbitListener`, indicando que este método será chamado sempre que houver uma mensagem na fila especificada.
  - A anotação `@Payload` indica que o parâmetro do método (`EmailRecordDto`) será preenchido com o corpo da mensagem recebida.
  - Dentro do método, a mensagem recebida é convertida para um objeto `EmailRecordDto`.

### **4. Processamento da Mensagem:**

```java
var emailModel = new EmailModel();
BeanUtils.copyProperties(emailRecordDto, emailModel);
emailService.sendEmail(emailModel);
```

- **Descrição:**
  - Um novo objeto `EmailModel` é criado para representar a mensagem recebida.
  - O método `copyProperties` da classe `BeanUtils` é utilizado para copiar as propriedades do `EmailRecordDto` para o `EmailModel`, facilitando a conversão entre os dois tipos.
  - O objeto `EmailModel` é então passado para o método `sendEmail` da instância de `EmailService` para processamento adicional.

### **5. Conclusão:**

A classe `EmailConsumer` desempenha um papel vital ao consumir mensagens da fila RabbitMQ e encaminhá-las para a camada de serviço (`EmailService`) para processamento adicional, como o envio efetivo de e-mails. Ao usar a anotação `@RabbitListener`, essa classe integra-se perfeitamente à infraestrutura de mensageria assíncrona fornecida pelo RabbitMQ, garantindo uma comunicação eficiente e assíncrona entre os diferentes microservices.

# Feito por: `Daniel Penelva de Andrade`
