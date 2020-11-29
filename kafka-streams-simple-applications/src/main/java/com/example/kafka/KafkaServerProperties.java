package com.example.kafka;

public class KafkaServerProperties {

    public static final String BOOTSTRAP_SERVERS = "127.0.0.1:9092";

    public static final String INPUT_TOPIC = "streams-plaintext-input";

    public static final String PIPE_OUTPUT_TOPIC = "streams-pipe-output";

    public static final String LINESPLIT_OUTPUT_TOPIC = "streams-linesplit-output";

    public static final String WORDCOUNT_OUTPUT_TOPIC = "streams-wordcount-output";

}
