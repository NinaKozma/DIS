# SOCIAL MEDIA POST MANAGEMENT MICROSERVICES

SOCIAL MEDIA POST MANAGEMENT - MICROSERVICES is a post management information service. It is a microservice architecture, implemented using **Spring Boot** and **Spring Cloud**. Business logic is presented through five microservices, where post, reaction, comment and image microservices represent core microservices, and post-composite represents integration of the core microservices. 

## Exam assignment

The microservice system responds to the defined project task.
A microservice system should:

- be developed using Spring Cloud and contain all the microservice components necessary for the functioning of such a system in production,]
- each system component is containerized (microservice, database, etc.),
- contains at least 5 microservices related to the application's business logic,
- to support different communications (synchronous/asynchronous) according to business logic,
- includes unit and integration tests and
- the pipeline (build/test/deploy) is clearly defined and configured.


## Prerequisites

In order to successfully complete the defined task, following prerequisites needs to be implemented:
- Spring STS - Spring Tool Suite (STS) is an Integrated Development Environment (IDE) based on Eclipse and specifically designed for developing Spring Framework-based applications. The download can be made via <a href="https://spring.io/tools" target="_blank">this link</a>.
- Java – can be downloaded and installed from <a href="https://www.oracle.com/java/technologies/downloads/" target="_blank">this link</a>.
- Spring Boot CLI - Command-line tool for Spring Boot applications.  Download steps for installing  Spring Boot CLI can be found <a href="https://docs.spring.io/spring-boot/docs/current/reference/html/getting-started.html" target="_blank">here</a>.
- curl - Command line tool for testing HTTP-based API can be downloaded and installed from <a href="https://curl.se/download.html" target="_blank">this link</a>.
- jq -  Command line utility that is easily used to extract data from JSON documents. The download can be made via <a href="https://stedolan.github.io/jq/download/" target="_blank">this link</a>.
- Docker and docker-compose - download steps for installing Docker Desktop can be found <a href="https://docs.docker.com/desktop/" target="_blank">here</a>.

## Persistence

Persisting data in various types of databases is enabled using **Spring Data**. In this project, Spring Data subprojects for **MongoDB** and JPA that have been mapped to a **MySQL database** are used. In the microservice landscape, there are four microservices which require permanently storing data, those microservices are the four core microservices and they have the following DBMSs:
- Post - MongoDB
- Reaction - MongoDB
- Comment - MySQL
- Image – MySQL.

Java mapper tool, MapStruct, is also used to transform between Spring Data entity objects and the model API classes. The entity classes are similar to the API model classes in a term of what fields they contain. The database schema can be seen in the following picture along with all the attributes and data types:
<br />
<br />
![plot](https://github.com/NinaKozma/DIS/blob/master/2-basic-rest-services/diagrams/DIS_Class_Diagram.png)
<br />
<br />
## Reactive microservices

The logic for **retrieving data** (GET request) for the post composite microservice is a **non-blocking synchronous** call to all four core microservices since there is an end user waiting for response. APIs in the core services are non-blocking as well. To make services reactive, programming model **Project Reactor** is used. The core data types in Project Reactor are <em>Mono object</em> (process 0..1 element) and <em>Flux</em> (process a stream of 0..N elements). The MongoDB-based services is made reactive, amongst others, using <em>ReactiveCrudRepository</em> interface.  In the case of services which uses JPA to access its data in a relational database, there is no support for non-blocking programming model, so we run blocking code using **Scheduler**. 
<br />
<br /> 
In contrast to the approach used for the retrieving of data, for **creating and deleting**, an **event-driven asynchronous approach** is used. The composite service will publish create and delete events on each core service topic and return 200 OK without waiting the core services to process their message. To implement an event-driven asynchronous services, **Spring Cloud Stream** is used. The core concept in Spring Cloud Stream are <em>message</em>, <em>publisher</em>, <em>subscriber</em>, <em>channel</em> and <em>binder</em>. To able composite service to publish events on different topic, one <em>MessageChannel</em> per topic in the <em>MessageSource</em> interface is declared. Injected <em>MessageSource</em> object is used to publish event on a topic using the method to get a message channel and then <em>send()</em> method. <em>MessageBuilder</em> is used to create a message. To be able to consume events in core services, <em>MessageProcessor</em> that listen for event is declared. <em>MessageProcessor</em> for MongoDB-based services is based on **blocking programming model**. Messaging systems that have been used in this project are **RabbitMQ and Apache Kafka**.

## Microservice landscape

Microservice landscape is attached in the following image.
<br />
<br />
![plot](https://github.com/NinaKozma/DIS/blob/master/2-basic-rest-services/diagrams/DIS_Microservice_Landscape_Diagram.png)
<br />
<br />
**SpringCloud** is used to make services production-ready, scalable, robust, configurable, secure and resilient. SpingCloud is used to implement the following design pattern:
- **Service Discovery using Netflix Eureka** – Service Discovery keeps track of currently available microservices and the IP addressess of its instances.
- **Edge server using Spring Cloud Gateway** – The gateway provides entry point to the microservice landscape, and it is used for routing to APIs. Some of the microservices are exposed to the outside of the system landscape (service discovery and composite services) and remaining of them (core services) is hidden from external access. The exposed microservices must be protected against requests from malicious clients.
- **Reactive microservices** that are previously described.
- **Central configuration using Spring Cloud Configuration Server** – Central configuration is used to centralize managing the configuration of microservices. Configuration files for microservices are placed in a central configuration repository in the local memory.
- **Circuit breaker using Resilience4j and retry mechanism** – Circuit breaker and retry mechanism is used to make microservices resilient, that is, how to mitigate and recover from error.
- **Distributed tracing using Spring Cloud Sleuth and Zipkin** - Distributed tracing is user to track and visualize how requests and messages flow between microservices when processing an external call to the system landscape.
- **Security access to APIs using OAuth 2.0 and OpenID Connect**

## Pipeline (build/test)

```bash
  ./gradlew build && docker-compose build && docker-compose up
```
If the microservice landscape is running, it is possible to execute the test script with the command:<br />
```bash ./test-em-all.bash```,<br /> or if it isn't in running state, you can execute test script with:<br /> ```bash./test-em-all.bash start```<br />
