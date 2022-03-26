package com.dam0.springbootelasticsearch.handler;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.ErrorHandler;

public class KafkaErrorHandler implements ErrorHandler {

    @Override
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {

    }
}

