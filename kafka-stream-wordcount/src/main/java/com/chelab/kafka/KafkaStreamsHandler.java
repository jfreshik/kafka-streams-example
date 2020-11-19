package com.chelab.kafka;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.KafkaStreams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class KafkaStreamsHandler {

    @Value("${spring.kafka.topics.input.name}")
    private String inputTopic;

    private final KafkaStreams kafkaStreams;
    private final KafkaTemplate<String, String> kafkaTemplate;

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
}
