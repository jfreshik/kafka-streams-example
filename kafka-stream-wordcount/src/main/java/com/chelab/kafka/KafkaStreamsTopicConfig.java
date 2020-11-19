package com.chelab.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaStreamsTopicConfig {
    @Value(value = "${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value(value = "${spring.kafka.topics.input.name}")
    private String inputTopicName;

    @Value(value = "${spring.kafka.topics.input.partitions}")
    private int inputTopicPartitions;

    @Value(value = "${spring.kafka.topics.input.replicas}")
    private int inputTopicReplicas;

    @Value(value = "${spring.kafka.topics.output.name}")
    private String outputTopicName;

    @Value(value = "${spring.kafka.topics.output.partitions}")
    private int outputTopicPartitions;

    @Value(value = "${spring.kafka.topics.output.replicas}")
    private int outputTopicReplicas;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic createInputTopic() {
        // topic-name, partition, replication-factor
        return TopicBuilder.name(inputTopicName)
                .partitions(inputTopicPartitions)
                .replicas(outputTopicReplicas)
                .build();
    }

    @Bean
    public NewTopic createOutputTopic() {
        // topic-name, partition, replication-factor
        return TopicBuilder.name(outputTopicName)
                .partitions(outputTopicPartitions)
                .replicas(outputTopicReplicas)
                .build();
    }
}
