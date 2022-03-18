package com.dam0.springbootelasticsearch.service;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.Alias;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class IndexAPIs {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexAPIs.class);
    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public IndexAPIs(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public IndexResponse index(String index, Map<String, Object> source) throws IOException {
        IndexRequest request = new IndexRequest()
                .index(index)
                .source(source, XContentType.JSON)
                .timeout(TimeValue.timeValueSeconds(30));

        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        LOGGER.debug(response.toString());

        return response;
    }

    public void indexAsync(String index, Map<String, Object> source) {
        IndexRequest request = new IndexRequest()
                .index(index)
                .source(source, XContentType.JSON)
                .timeout(TimeValue.timeValueSeconds(30));

        restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(IndexResponse response) {
                LOGGER.debug("ASYNC SUCCESS");
                LOGGER.debug(response.toString());
            }

            @Override
            public void onFailure(Exception e) {
                LOGGER.debug("ASYNC FAILURE", e);
                LOGGER.warn("ASYNC REQUEST ERROR : {}", e.getMessage());
            }
        });
    }

    public CreateIndexResponse createIndex(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);

        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2)
        );

        // request mapping - (1)
        /*
        request.mapping(
                "{\n" +
                        "  \"properties\": {\n" +
                        "    \"message\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                XContentType.JSON);
         */

        // request mapping - (2)
        Map<String, Object> message = new HashMap<>();
        message.put("type", "text");
        Map<String, Object> properties = new HashMap<>();
        properties.put("message", message);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        request.mapping(mapping);

        request.alias(new Alias("index_alias").filter(QueryBuilders.termQuery("user", "dam0")));

        return restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    public void createIndexAsync(String index, Map<String, Object> source) {
        CreateIndexRequest request = new CreateIndexRequest(index);

        request.source("{\n" +
                "    \"settings\" : {\n" +
                "        \"number_of_shards\" : 1,\n" +
                "        \"number_of_replicas\" : 0\n" +
                "    },\n" +
                "    \"mappings\" : {\n" +
                "        \"properties\" : {\n" +
                "            \"message\" : { \"type\" : \"text\" }\n" +
                "        }\n" +
                "    },\n" +
                "    \"aliases\" : {\n" +
                "        \"index_alias\" : {}\n" +
                "    }\n" +
                "}", XContentType.JSON);

        restHighLevelClient.indices().createAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(CreateIndexResponse response) {
                LOGGER.debug("ASYNC SUCCESS");
                LOGGER.debug(response.toString());
            }

            @Override
            public void onFailure(Exception e) {
                LOGGER.debug("ASYNC FAILURE", e);
                LOGGER.warn("ASYNC REQUEST ERROR : {}", e.getMessage());
            }
        });
    }

}
