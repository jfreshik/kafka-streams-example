# Kafka Streams use cases


* https://kafka.apache.org/26/documentation/streams/tutorial
* Kafka Stream 라이브러리만 이용한 stream 샘플
```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
    <version>2.6.0</version>
</dependency>
```

## Apps
* Pipe: stream bypass
* LineSplit: flatMapValues Process
* WordCount: Materialize result(state store)


## Pipe app

* stream build
```java
final StreamsBuilder builder = new StreamsBuilder();
builder.stream(KafkaServerProperties.INPUT_TOPIC).to(KafkaServerProperties.PIPE_OUTPUT_TOPIC);
```

* stream topology
```shell script
[com.example.kafka.Pipe.main()] INFO com.example.kafka.Pipe - Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [streams-plaintext-input])
      --> KSTREAM-SINK-0000000001
    Sink: KSTREAM-SINK-0000000001 (topic: streams-pipe-output)
      <-- KSTREAM-SOURCE-0000000000
```

## LineSplit app

* stream build
```java
final StreamsBuilder builder = new StreamsBuilder();
final KStream<String, String> source = builder.stream(KafkaServerProperties.INPUT_TOPIC);
source.flatMapValues(value -> Arrays.asList(value.split("\\W+")))
        .to(KafkaServerProperties.LINESPLIT_OUTPUT_TOPIC);
```

* stream topology
```shell script
[com.example.kafka.LineSplit.main()] INFO com.example.kafka.LineSplit - Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [streams-plaintext-input])
      --> KSTREAM-FLATMAPVALUES-0000000001
    Processor: KSTREAM-FLATMAPVALUES-0000000001 (stores: [])
      --> KSTREAM-SINK-0000000002
      <-- KSTREAM-SOURCE-0000000000
    Sink: KSTREAM-SINK-0000000002 (topic: streams-linesplit-output)
      <-- KSTREAM-FLATMAPVALUES-0000000001

```

## WordCount app

* stream build
```java
final StreamsBuilder builder = new StreamsBuilder();
final KStream<String, String> source = builder.stream(KafkaServerProperties.INPUT_TOPIC);
source.flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))
        .groupBy((key, value) -> value)
        .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as("counts-store"))
        .toStream()
        .to(KafkaServerProperties.WORDCOUNT_OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));
```

* stream toplogy
```shell script
[com.example.kafka.LineSplit.main()] INFO com.example.kafka.LineSplit - Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [streams-plaintext-input])
      --> KSTREAM-FLATMAPVALUES-0000000001
    Processor: KSTREAM-FLATMAPVALUES-0000000001 (stores: [])
      --> KSTREAM-SINK-0000000002
      <-- KSTREAM-SOURCE-0000000000
    Sink: KSTREAM-SINK-0000000002 (topic: streams-linesplit-output)
      <-- KSTREAM-FLATMAPVALUES-0000000001

```


## 실행
```shell script
# 빌드
$ mvnw clean package

# Pipe App
$ mvnw exec:java -Dexec.mainClass=com.example.kafka.Pipe

# LineSplit App
$ mvnw exec:java -Dexec.mainClass=com.example.kafka.LineSplit

# WordCount App
$ mvnw exec:java -Dexec.mainClass=com.example.kafka.WordCount
```
