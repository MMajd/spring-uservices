### Microservices practise 
- one commend to build and run all, should be executed from project directory
```
./gradlew build && docker-compose build && docker-compose up
``` 

- App implements api composition patter, will migrate to cqrs in future
- App implements basic event based inter-process-communication, will migrate to event-sourcing in feature
- App contains RabbitMQ as default broker but also Kafka config as added in the cloud string section for feature migration
  - Preference of RabbitMQ over Kafka just for the Built-in WebUI in the managment release 

- To query health endpoint in terminal use curl & jq (json processor) 
```
curl -s localhost:8080/actuator/health | jq .  
```


- After build completion
    - make sure that you have docker and docker-compose installed on your machine
    - and make sure that you have enough resources to run all services
    - type in your terminal ```docker-compose up``` and docker compose will take of the rest

- You can view the api docs on ```http://localhost:8080/openapi/swagger-ui.html```


...to be cntd
