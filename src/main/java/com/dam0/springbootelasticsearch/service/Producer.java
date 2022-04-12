package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import com.dam0.springbootelasticsearch.dto.IndexDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Slf4j
@Service
public class Producer {
    public static final String BASIC_TOPIC = "basic-topic";      // partition 20개로 맞추기
    public static final String FINAL_TOPIC = "final-topic";      // partition 20개로 맞추기
    private final KafkaTemplate<String, IndexDto> basicKafkaTemplate;
    private final KafkaTemplate<String, IndexDto> finalKafkaTemplate;
    private final IndexAPIs indexAPIs;

    @Autowired
    public Producer(@Qualifier("basic") KafkaTemplate<String, IndexDto> basicKafkaTemplate,
                    @Qualifier("final") KafkaTemplate<String, IndexDto> finalKafkaTemplate,
                    IndexAPIs indexAPIs) {
        this.basicKafkaTemplate = basicKafkaTemplate;
        this.finalKafkaTemplate = finalKafkaTemplate;
        this.indexAPIs = indexAPIs;
    }

    /**
     * 들어온 로그를 Kafka 토픽으로 보내기
     */
    public void publishToTopic(IndexDto indexDto, Map<String, Object> source, boolean isFinal) {

        ListenableFuture<SendResult<String, IndexDto>> future;

        if (isFinal) {
            future = finalKafkaTemplate.send(FINAL_TOPIC, indexDto);
        } else {
            future = basicKafkaTemplate.send(BASIC_TOPIC, indexDto);
        }

        // 비동기 처리를 위한 콜백함수.
        future.addCallback(new ListenableFutureCallback<SendResult<String, IndexDto>>() {
            @Override
            public void onFailure(@NotNull Throwable throwable) {
                log.error("## FAILED PRODUCING TO KAFKA");
                log.error("# indexDto = {}, Error Message = {}", indexDto, throwable.getMessage());
                // TODO: FailBack
                indexAPIs.indexAsync(indexDto.getIndex(), source);
            }

            @Override
            public void onSuccess(SendResult<String, IndexDto> sendResult) {
                log.debug("[>>>] Published message=[" + indexDto + "] with offset=[" + sendResult.getRecordMetadata().offset() + "]");
            }
        });

    }
}
