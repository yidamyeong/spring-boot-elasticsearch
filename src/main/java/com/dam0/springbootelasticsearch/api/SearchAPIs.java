package com.dam0.springbootelasticsearch.api;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SearchAPIs {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchAPIs.class);
    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public SearchAPIs(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public SearchResponse search(String index, QueryBuilder query, String sortKey, int page, int size) throws IOException {
        SearchRequest searchRequest = new SearchRequest()
                .indices(index)
                .source(new SearchSourceBuilder()
                        .query(query)
                        .sort(sortKey)
                        .from((page - 1) * size)
                        .size(size)
                );
        LOGGER.debug("SEARCH SOURCE : \n{}", searchRequest.source());

        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }
}
