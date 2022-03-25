package com.dam0.springbootelasticsearch.api;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SearchAPIs {

    private final RestHighLevelClient restHighLevelClient;

    @Autowired
    public SearchAPIs(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public SearchResponse search(String index, QueryBuilder query, String sortKey, Integer page, Integer size) throws IOException {
        SearchRequest searchRequest = new SearchRequest()
                    .indices(index)
                    .source(new SearchSourceBuilder()
                            .query(query)
                            .sort(sortKey)
                            .from((page - 1) * size)
                            .size(size)
                    );

        log.debug("SEARCH SOURCE : \n{}", searchRequest.source());

        return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
    }
}
