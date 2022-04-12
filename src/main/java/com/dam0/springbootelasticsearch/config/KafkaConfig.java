package com.dam0.springbootelasticsearch.config;

import com.dam0.springbootelasticsearch.dto.IndexDto;
import com.dam0.springbootelasticsearch.handler.KafkaErrorHandler;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer2;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return props;
    }

    /** Basic Producer Configuration for docc-etl **/
    @Bean
    public ProducerFactory<String, IndexDto> basicProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    @Qualifier("basic")
    public KafkaTemplate<String, IndexDto> basicKafkaTemplate() {
        return new KafkaTemplate<>(basicProducerFactory());
    }


    /** Final Producer Configuration for docc-etl-final **/
    @Bean
    public ProducerFactory<String, IndexDto> finalProducerFactory() {
        return new DefaultKafkaProducerFactory<>(producerConfigs());
    }

    @Bean
    @Qualifier("final")
    public KafkaTemplate<String, IndexDto> finalKafkaTemplate() {
        return new KafkaTemplate<>(finalProducerFactory());
    }



    /**
     *  Consumer Configuration
     */
//    @Bean
//    public Map<String, Object> consumerConfigs() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
//        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, "wehago-logsystem"); //
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//
//        return props;
//    }
//
//    // TODO: 다시 쓰기. 버전 차이 있음
//    @Bean
//    public ConsumerFactory<String, IndexDto> consumerFactory() {
//        ErrorHandlingDeserializer2<IndexDto> errorHandlingDeserializer
//                = new ErrorHandlingDeserializer2<IndexDto>(new JsonDeserializer<>(IndexDto.class));
//        return new DefaultKafkaConsumerFactory<>(consumerConfigs()
//                , new StringDeserializer()
//                , errorHandlingDeserializer);
//    }
//
//    @Bean
//    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, IndexDto>> kafkaListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, IndexDto> factory =
//                new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConcurrency(2);
//        factory.setConsumerFactory(consumerFactory());
//        factory.setErrorHandler(new KafkaErrorHandler());
//        return factory;
//    }

}
