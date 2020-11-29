package com.example.kafka;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Pipe {

	public static void main(String[] args) {

		Logger logger = LoggerFactory.getLogger(Pipe.class);
		logger.info("--------- PIPE APP ----------");

		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-pipe");
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaServerProperties.BOOTSTRAP_SERVERS);
		props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

		final StreamsBuilder builder = new StreamsBuilder();
		builder.stream(KafkaServerProperties.INPUT_TOPIC).to(KafkaServerProperties.PIPE_OUTPUT_TOPIC);

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
			logger.info(">>> PIPE STARTED! <<<");
			latch.await();
		} catch (Throwable e) {
			System.exit(1);
		}


		System.exit(0);
	}

}
