package com.dam0.springbootelasticsearch.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${primary.elasticsearch.host}")
    private String primaryHost;

    @Value("${primary.elasticsearch.port}")
    private int primaryPort;

    @Value("${secondary.elasticsearch.host}")
    private String secondaryHost;

    @Value("${secondary.elasticsearch.port}")
    private int secondaryPort;

    @Bean
    @Qualifier("primary")
    public RestHighLevelClient primaryRestHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(primaryHost, primaryPort, "http")
                )
        );
    }

    @Bean
    @Qualifier("secondary")
    public RestHighLevelClient secondaryRestHighLevelClient() {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(secondaryHost, secondaryPort, "http")
                )
        );
    }
}

