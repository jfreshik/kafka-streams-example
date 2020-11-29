# KafkaStreams - WordCount DEMO

* https://kafka.apache.org/26/documentation/streams/quickstart 를 참조해서 REST API로 동작 확인할 수 있도록 구현함

* KTable 에 key,value 저장
  * key = word
  * value = count
* KStream 은 downstream

## Producer,Consumer 설정
* AutoConfiguration 으로 producer - KafkaTemplate, consumer - ConsumerFactory 설정
```yaml
spring:
  kafka:
    bootstrap-servers: 127.0.0.1:9092

    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.LongDeserializer
      auto-offset-reset: earliest

    topics:
      input:
        name: streams-plaintext-input
        partitions: 1
        replicas: 1

      output:
        name: streams-wordcount-output
        partitions: 1
        replicas: 1

```


## Word Count 스트림 생성
```java
    @Bean
    public KafkaStreams wordCountStream() {
		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> source = builder.stream(inputTopic);
		KTable<String, Long> counts = source
				.flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))	// 1
				.peek((key, value) -> System.out.println(" > KEY: " + key + ", VALUE: " + value))   // 2
				.groupBy((key, value) -> value)     // 3
				.count();       // 4

		counts.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));
        
        final Topology topology = builder.build();
		System.out.println("TOPOLOGY: " + topology.describe()); // topology 확인
		return new KafkaStreams(topology, kafkaStreamConfig());
	}
```

> 1. 문자열을 공백으로 구분하여 WORD 리스트로 분리 (split \\W+) / flatMapValues
> 1. 디버깅용: flatmap Key,Value 값 확인 (key 는 null, value는 WORD)
> 1. WORD 단위로 grouping
> 1. 그룹 카운트


* Topology describe 결과
```shell script
Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [streams-plaintext-input])
      --> KSTREAM-FLATMAPVALUES-0000000001
    Processor: KSTREAM-FLATMAPVALUES-0000000001 (stores: [])
      --> KSTREAM-PEEK-0000000002
      <-- KSTREAM-SOURCE-0000000000
    Processor: KSTREAM-PEEK-0000000002 (stores: [])
      --> KSTREAM-KEY-SELECT-0000000003
      <-- KSTREAM-FLATMAPVALUES-0000000001
    Processor: KSTREAM-KEY-SELECT-0000000003 (stores: [])
      --> KSTREAM-FILTER-0000000007
      <-- KSTREAM-PEEK-0000000002
    Processor: KSTREAM-FILTER-0000000007 (stores: [])
      --> KSTREAM-SINK-0000000006
      <-- KSTREAM-KEY-SELECT-0000000003
    Sink: KSTREAM-SINK-0000000006 (topic: KSTREAM-AGGREGATE-STATE-STORE-0000000004-repartition)
      <-- KSTREAM-FILTER-0000000007

  Sub-topology: 1
    Source: KSTREAM-SOURCE-0000000008 (topics: [KSTREAM-AGGREGATE-STATE-STORE-0000000004-repartition])
      --> KSTREAM-AGGREGATE-0000000005
    Processor: KSTREAM-AGGREGATE-0000000005 (stores: [KSTREAM-AGGREGATE-STATE-STORE-0000000004])
      --> KTABLE-TOSTREAM-0000000009
      <-- KSTREAM-SOURCE-0000000008
    Processor: KTABLE-TOSTREAM-0000000009 (stores: [])
      --> KSTREAM-SINK-0000000010
      <-- KSTREAM-AGGREGATE-0000000005
    Sink: KSTREAM-SINK-0000000010 (topic: streams-wordcount-output)
      <-- KTABLE-TOSTREAM-0000000009
```

## KafkaStreams 시작/종료

* REST API 를 통해 kafkaStreams 시작/종료 처리

```java
    /**
     * KAFKA STREAMS 시작
     */
    @PostMapping("/streams/on")
    public String streamsOn() {
        if (!kafkaStreams.state().isRunningOrRebalancing()) {
            kafkaStreams.start();
        }
        return "ON";
    }

    /**
     * KAFKA STREAMS 종료
     */
    @PostMapping("/streams/off")
    public String streamsOff() {
        if (kafkaStreams.state().isRunningOrRebalancing()) {
            kafkaStreams.close();
        }
        return "OFF";
    }
```

```shell script
$ curl -X POST localhost:8080/streams/on
$ curl -X POST localhost:8080/streams/off
```


## WordCount 할 문자열 입력

* REST API를 통해 문자열 입력
```java
    /**
     * streams-plaintext-input 토픽에 문자열 입력
     * @param words     공백이 포함 된 문자열
     */
    @PostMapping("/streams/input")
    public String insertWord(@RequestParam String words) {

        final ListenableFuture<SendResult<String, String>> future = kafkaTemplate.send(inputTopic, words);

        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                System.out.println("FAILED: " + throwable.toString());
            }

            @Override
            public void onSuccess(SendResult<String, String> stringStringSendResult) {
                System.out.println("Sent words: [" + words + "]");
            }
        });

        return "OK";
    }
```

```shell script
$ curl -X POST 'localhost:8080/streams/input' --data-urlencode "words=hello world"
```

## WordCount 결과 consume
```java
    /**
     * streams-wordcount-output 토픽 consume
     * @param count
     * @param word
     */
    @KafkaListener(topics = "${spring.kafka.topics.output.name}", groupId = "streams-wordcount-consumer-group")
    public void streamReceived(@Payload Long count,
                               @Header(KafkaHeaders.RECEIVED_MESSAGE_KEY) String word) {
        System.out.println("> " + word + ": " + count);
    }
```
`@Payload Long count` 에 누적 카운트 값 수신한다.

> `@Header(KafkaHeaders.RECEIVED_MESSAGE_KEY)` 와 같이 KafkaHeaders를 이용하면 값 이외에 다양한 정보 수신 가능

```java
 public abstract class KafkaHeaders {
     public static final String PREFIX = "kafka_";
     public static final String RECEIVED = "kafka_received";
     public static final String TOPIC = "kafka_topic";
     public static final String MESSAGE_KEY = "kafka_messageKey";
     public static final String PARTITION_ID = "kafka_partitionId";
     public static final String OFFSET = "kafka_offset";
     public static final String RAW_DATA = "kafka_data";
     public static final String RECORD_METADATA = "kafka_recordMetadata";
     public static final String ACKNOWLEDGMENT = "kafka_acknowledgment";
     public static final String CONSUMER = "kafka_consumer";
     public static final String RECEIVED_TOPIC = "kafka_receivedTopic";
     public static final String RECEIVED_MESSAGE_KEY = "kafka_receivedMessageKey";
     public static final String RECEIVED_PARTITION_ID = "kafka_receivedPartitionId";
     public static final String TIMESTAMP_TYPE = "kafka_timestampType";
     public static final String TIMESTAMP = "kafka_timestamp";
     public static final String RECEIVED_TIMESTAMP = "kafka_receivedTimestamp";
     public static final String NATIVE_HEADERS = "kafka_nativeHeaders";
     public static final String BATCH_CONVERTED_HEADERS = "kafka_batchConvertedHeaders";
     public static final String CORRELATION_ID = "kafka_correlationId";
     public static final String REPLY_TOPIC = "kafka_replyTopic";
     public static final String REPLY_PARTITION = "kafka_replyPartition";
     public static final String DLT_EXCEPTION_FQCN = "kafka_dlt-exception-fqcn";
     public static final String DLT_EXCEPTION_STACKTRACE = "kafka_dlt-exception-stacktrace";
     public static final String DLT_EXCEPTION_MESSAGE = "kafka_dlt-exception-message";
     public static final String DLT_ORIGINAL_TOPIC = "kafka_dlt-original-topic";
     public static final String DLT_ORIGINAL_PARTITION = "kafka_dlt-original-partition";
     public static final String DLT_ORIGINAL_OFFSET = "kafka_dlt-original-offset";
     public static final String DLT_ORIGINAL_TIMESTAMP = "kafka_dlt-original-timestamp";
     public static final String DLT_ORIGINAL_TIMESTAMP_TYPE = "kafka_dlt-original-timestamp-type";
     public static final String GROUP_ID = "kafka_groupId";
     public static final String DELIVERY_ATTEMPT = "kafka_deliveryAttempt";
 
     public KafkaHeaders() {
     }
 }

```