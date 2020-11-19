package com.chelab.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class LineSplit {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(LineSplit.class);
        logger.info("--------- LineSplit APP ----------");

        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-linesplit");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaServerProperties.BOOTSTRAP_SERVERS);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        final StreamsBuilder builder = new StreamsBuilder();
        final KStream<String, String> source = builder.stream(KafkaServerProperties.INPUT_TOPIC);
        source.flatMapValues(value -> Arrays.asList(value.split("\\W+")))
                .to(KafkaServerProperties.LINESPLIT_OUTPUT_TOPIC);

        final Topology topology = builder.build();
        logger.info("{}" , topology.describe());

        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            logger.info(">>> LINESPLIT STARTED! <<<");
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }


        System.exit(0);
    }
}
