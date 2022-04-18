package com.dam0.springbootelasticsearch.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexAPIs {

    private final RestHighLevelClient restHighLevelClient;

    public static IndexResponse index(String index, Map<String, Object> source,
                                      RestHighLevelClient restHighLevelClient) throws IOException {
        IndexRequest request = new IndexRequest()
                .index(index)
                .source(source, XContentType.JSON)
                .timeout(TimeValue.timeValueSeconds(30));

        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        log.debug(response.toString());

        return response;
    }

    public static void indexAsync(String index, Map<String, Object> source, RestHighLevelClient restHighLevelClient) {
        IndexRequest request = new IndexRequest()
                .index(index)
                .source(source, XContentType.JSON)
                .timeout(TimeValue.timeValueSeconds(30));

        restHighLevelClient.indexAsync(request, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(IndexResponse response) {
                log.debug("ASYNC SUCCESS");
                log.debug(response.toString());
            }

            @Override
            public void onFailure(Exception e) {
                log.error("ASYNC FAILURE", e);
                log.error("ASYNC REQUEST ERROR : {}", e.getMessage());
            }
        });
    }

    public static CreateIndexResponse createIndexSync(CreateIndexRequest request,
                                                      RestHighLevelClient restHighLevelClient) throws IOException {
        return restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
    }

    public static void createIndexAsync(String index, RestHighLevelClient restHighLevelClient) {
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
                log.debug("ASYNC SUCCESS");
                log.debug(response.toString());
            }

            @Override
            public void onFailure(Exception e) {
                log.error("ASYNC FAILURE", e);
                log.error("ASYNC REQUEST ERROR : {}", e.getMessage());
            }
        });
    }

}
