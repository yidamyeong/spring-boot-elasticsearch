package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import com.dam0.springbootelasticsearch.dto.IndexDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Service
public class Producer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    public static final String SAMPLE_TOPIC = "sample-topic";      // partition 20개로 맞추기
    private final KafkaTemplate<String, IndexDto> kafkaTemplate;
    private final IndexAPIs indexAPIs;

    @Autowired
    public Producer(KafkaTemplate<String, IndexDto> kafkaTemplate, IndexAPIs indexAPIs) {
        this.kafkaTemplate = kafkaTemplate;
        this.indexAPIs = indexAPIs;
    }

    /**
     * 들어온 로그를 Kafka 토픽으로 보내기
     */
    public void publishToTopic(IndexDto indexDto, Map<String, Object> source) {

        ListenableFuture<SendResult<String, IndexDto>> future = kafkaTemplate.send(SAMPLE_TOPIC, indexDto);

        // 비동기 처리를 위한 콜백함수.
        future.addCallback(new ListenableFutureCallback<SendResult<String, IndexDto>>() {
            @Override
            public void onFailure(@NotNull Throwable throwable) {
                LOGGER.error("## FAILED PRODUCING TO KAFKA");
                LOGGER.error("# indexDto = {}, Error Message = {}", indexDto, throwable.getMessage());
                // TODO: FailBack
                indexAPIs.indexAsync(indexDto.getIndex(), source);
            }

            @Override
            public void onSuccess(SendResult<String, IndexDto> sendResult) {
                LOGGER.debug("[>>>] Published message=[" + indexDto + "] with offset=[" + sendResult.getRecordMetadata().offset() + "]");
            }
        });

    }
}
