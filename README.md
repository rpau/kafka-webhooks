# Kafka Connect Webhooks

kafka-webhooks are Kafka Producer/Consumers for sending data from/to Kafka using third party REST services.   

# Prerequisites

- a Git client to fetch the project
- Docker Compose
- Apache Maven installed.
- git clone https://github.com/rpau/kafka-webhooks kafka-webhooks

# Quickstart

Start docker containers:

```
docker-compose up
```
Open this URL in your browser to check your system is up.

```
http://{YOUR_DOCKER_IP_ADDRESS}:8081 
```
# Post to Kafka with REST

The only thing you need is to send a REST call, which automatically will publish it as a Kafka message.

```
curl -X POST -d "{JSON PAYLOAD}" http://{YOUR_DOCKER_IP_ADDRESS}:8081/message/{TOPIC}
```

# Add a Kafka Consumer webhook

Add your webhook with this command:

```
curl -X POST -d "{\"url\": \"{YOUR_WEBHOOK_URL}\", \"topic\": \"{TOPIC}\"}" http://{YOUR_DOCKER_IP_ADDRESS}:8081/webhook
```

This will send the messages of the specified topic to the url that you have specified.

# Notes

This is a POC (Proof Of Concept) to decouple REST services from Kafka and thus, it is not yet scalable. 
The next step is managing/scaling Kafka Consumers through Kubernetes and Docker containers instead of
threads.