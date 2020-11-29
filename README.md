# Kafka Streams tutorials

## Run Kafka

* `wurstmeister/kafka` 도커 이미지로 Kafka 구동
    * docker: https://hub.docker.com/r/wurstmeister/kafka
    * github: https://github.com/wurstmeister/kafka-docker
 
```yaml
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
  kafka:
    image: wurstmeister/kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 127.0.0.1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181

```

```shell script
$ docker-compose up -d
```


## WordCount demo
* [Kafka Streams WordCount DEMO](kafka-stream-wordcount/README.md) : 
* [Kafka Streams application DEMO](kafka-streams-simple-applications/README.md)

## Reference
