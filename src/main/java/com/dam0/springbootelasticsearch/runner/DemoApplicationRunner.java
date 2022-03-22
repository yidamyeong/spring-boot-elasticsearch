package com.dam0.springbootelasticsearch.runner;

import com.dam0.springbootelasticsearch.api.SearchAPIs;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;

@Order(2)
@Component
public class DemoApplicationRunner implements ApplicationRunner {

    private List<String> fieldList;
    private final SearchAPIs searchAPIs;

    public DemoApplicationRunner(SearchAPIs searchAPIs) {
        this.searchAPIs = searchAPIs;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // keyword 붙이고, 안 붙이고의 차이에 대해.
        // size, page 미표기하여 전부 검색할 수는 없을까?
        MatchQueryBuilder query = QueryBuilders.matchQuery("field1.keyword", "sample-value");
        SearchResponse response = searchAPIs.search("sample-index", query, "field2.keyword", 1, 100);

        SearchHits searchHits = response.getHits();

        List<SearchHit> searchHitList = Arrays.stream(searchHits.getHits()).toList();
        List<String> fieldList = new ArrayList<>();
        for (SearchHit searchHit : searchHitList) {
            Map<String, Object> source = searchHit.getSourceAsMap();
            fieldList.add((String) source.get("sample-field"));
        }

        this.fieldList = fieldList;
    }

    public List<String> getList() {
        return fieldList;
    }

}
