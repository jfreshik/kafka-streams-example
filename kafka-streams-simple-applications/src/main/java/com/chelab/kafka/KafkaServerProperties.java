package com.chelab.kafka;

public class KafkaServerProperties {

    public static final String BOOTSTRAP_SERVERS = "10.6.120.56:9092,10.6.120.121:9092,10.6.120.67:9092";

    public static final String INPUT_TOPIC = "streams-plaintext-input";

    public static final String PIPE_OUTPUT_TOPIC = "streams-pipe-output";

    public static final String LINESPLIT_OUTPUT_TOPIC = "streams-linesplit-output";

    public static final String WORDCOUNT_OUTPUT_TOPIC = "streams-wordcount-output";

}
