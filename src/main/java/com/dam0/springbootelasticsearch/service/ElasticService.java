package com.dam0.springbootelasticsearch.service;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.core.TimeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class ElasticService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticService.class);
    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public ElasticService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public IndexResponse indexSync(String index, Map<String, Object> source) throws IOException {
        IndexRequest request = new IndexRequest()
                .index(index)
                .source(source, XContentType.JSON)
                .timeout(TimeValue.timeValueSeconds(30));

        IndexResponse response = restHighLevelClient.index(request, RequestOptions.DEFAULT);
        LOGGER.debug(response.toString());

        return response;
    }

    public void indexAsync(String index, Map<String, Object> source) {
        IndexRequest indexRequest = new IndexRequest()
                .index(index)
                .source(source, XContentType.JSON)
                .timeout(TimeValue.timeValueSeconds(30));

        restHighLevelClient.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                LOGGER.debug("ASYNC SUCCESS");
                LOGGER.debug(indexResponse.toString());
            }

            @Override
            public void onFailure(Exception e) {
                LOGGER.debug("ASYNC FAILURE", e);
                LOGGER.warn("ASYNC REQUEST ERROR : {}", e.getMessage());
            }
        });
    }
}
