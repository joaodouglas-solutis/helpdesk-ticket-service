# Helpdesk Ticket Service

Microsserviço responsável pelo ciclo de vida dos chamados.

## Arquitetura

Estrutura principal:

```text
com.solutis.ticketservice
├── config
├── controller
├── dto
├── entity
├── event
├── exception
├── repository
└── service
```

O serviço mantém o domínio de chamados isolado e utiliza componentes externos para informações de usuários e para propagação de eventos.

## Entidade Ticket

O domínio possui entidades e enums específicos:

```text
Ticket
Category
Priority
Status
```

Os estados e classificações do chamado são representados através de tipos próprios, evitando que essas informações sejam manipuladas como strings arbitrárias.

## TicketService

`TicketService` concentra o fluxo principal dos chamados:

```text
create
findById
findAll
update
assignTechnician
claim
close
delete
```

Além do `TicketRepository`, o serviço recebe:

```java
UserServiceClient
TicketEventPublisher
```

Isso coloca dentro da mesma operação de negócio tanto a validação das referências externas quanto a publicação das mudanças relevantes.

## Validação com User Service

O Ticket Service não mantém uma cópia da entidade de usuário.

Quando um chamado é criado, o serviço consulta o User Service para validar o cliente.

São verificadas:

```text
active
role
```

O mesmo mecanismo é utilizado para validar o técnico durante uma atribuição.

Assim, o Ticket Service mantém somente os identificadores necessários:

```text
customerId
technicianId
```

enquanto a responsabilidade sobre identidade permanece no User Service.

## Regra para clientes

Na criação de um chamado, usuários com:

```text
ROLE_CLIENT
```

não podem escolher arbitrariamente o `customerId`.

O código substitui o valor recebido pelo ID presente na autenticação:

```java
customerId = authenticatedUserId;
```

Isso também acontece nas consultas. Quando o usuário é cliente, o filtro é forçado para seu próprio identificador.

## Specifications

Os filtros de chamados foram isolados em `TicketSpecifications`.

Existem specifications para:

```text
isActive
hasStatus
hasPriority
hasCategory
belongsToCustomer
titleOrDescriptionContains
```

A consulta é composta dinamicamente através de:

```java
Specification.allOf(...)
```

Cada filtro retorna `null` quando não foi informado, permitindo que os critérios sejam combinados sem construir várias consultas diferentes no repository.

A pesquisa textual procura simultaneamente em:

```text
title
description
```

e utiliza `lower()` tanto no campo quanto no valor pesquisado para realizar uma busca sem diferenciação de maiúsculas e minúsculas.

## Estados e eventos

Quando um chamado é atualizado, o estado anterior é armazenado:

```java
Status previousStatus = ticket.getStatus();
```

Depois da alteração, o serviço compara:

```java
if (previousStatus != savedTicket.getStatus())
```

Somente quando existe mudança real de status o evento é publicado.

Isso evita emitir um evento de alteração de status quando o restante do chamado foi alterado sem mudança de estado.

## RabbitMQ

`TicketEventPublisher` centraliza a publicação dos eventos.

São publicados:

```text
TicketCreatedEvent
TicketAssignedEvent
TicketStatusChangedEvent
```

Todos são enviados para:

```text
ticket.exchange
```

utilizando routing keys específicas.

O publisher recebe a entidade `Ticket`, mas cria objetos de evento específicos antes de enviá-los.

Dessa forma, o conteúdo enviado ao RabbitMQ é um contrato próprio de integração, e não a entidade JPA.

## Atribuição de técnico

Existem dois fluxos distintos:

```text
assignTechnician()
claim()
```

`assignTechnician()` recebe explicitamente o técnico escolhido.

`claim()` obtém o técnico diretamente da autenticação:

```java
UUID technicianId =
    UUID.fromString(authentication.getName());
```

Nos dois casos, o User Service é consultado para validar se o usuário:

```text
está ativo
e
possui ROLE_TECHNICIAN
```

## Inativação de chamados

A exclusão também é lógica:

```java
ticket.setActive(false);
```

A busca interna utiliza:

```java
.findById(id)
.filter(Ticket::isActive)
```

Assim, registros inativados deixam de participar das operações normais sem serem removidos fisicamente.

## Transações

As operações de escrita do serviço são marcadas com:

```java
@Transactional
```

Enquanto consultas utilizam:

```java
@Transactional(readOnly = true)
```

A transação acompanha a operação de negócio, incluindo persistência e publicação dos eventos correspondentes.
