package com.chelab.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

@Configuration
public class KafkaStreamsConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topics.input.name}")
    private String inputTopic;

    @Value("${spring.kafka.topics.output.name}")
    private String outputTopic;

	private Properties kafkaStreamConfig() {
		final Properties props = new Properties();

		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
		props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);

		// Serdes (Serializer/Deserializer)
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

		// 처음 데이터 부터 consume
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
		return props;
	}

	@Bean
    public KafkaStreams wordCountStream() {
		StreamsBuilder builder = new StreamsBuilder();

		KStream<String, String> source = builder.stream(inputTopic);

		// 1. 문자열을 공백으로 구분하여 WORD 리스트로 분리 (split \\W+) / flatMapValues
		// 2. 디버깅용: flatmap Key,Value 값 확인 (key 는 null, value는 WORD)
		// 3. WORD 단위로 grouping
		// 4. 그룹 카운트
		KTable<String, Long> counts = source
				.flatMapValues(value -> Arrays.asList(value.toLowerCase(Locale.getDefault()).split("\\W+")))	// 1
				.peek((key, value) -> System.out.println(" > KEY: " + key + ", VALUE: " + value))					// 2
				.groupBy((key, value) -> value)																		// 3
				.count();																							// 4

		counts.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()));

		final Topology topology = builder.build();
		System.out.println(topology.describe());
		return new KafkaStreams(topology, kafkaStreamConfig());
	}
}
