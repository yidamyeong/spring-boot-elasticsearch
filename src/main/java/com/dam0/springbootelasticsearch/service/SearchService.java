package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.dto.SearchDto;
import com.dam0.springbootelasticsearch.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final RestHighLevelClient restHighLevelClient;

    public String search(SearchDto searchDto) throws Exception {
        // 검색 조건에 _type 넣지 않기
        SearchRequest searchRequest = new SearchRequest()
                .indices(searchDto.getIndex())
                .source(new SearchSourceBuilder()
                        .query(searchDto.getQuery())
                        .sort(searchDto.getDatefield(), SortOrder.DESC)
                        .from((searchDto.getPage() - 1) * searchDto.getSize())
                        .size(searchDto.getSize())
                );
        log.debug("SOURCE : \n" + searchRequest.source());

        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        return response.toString();
    }

    /**
     * 시리얼넘버 유효성 검증
     */
    public boolean isSerialNumberValid(String serialNumber) throws Exception {
        ValidationUtil.rejectIfEmptyOrWhitespace(serialNumber);

        String[] strings = serialNumber.split("__");
        long timestamp = Long.parseLong(strings[0]);

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM");
        DateTime dateTime = new DateTime(timestamp);
        String yyMM = dateTime.toString(dateTimeFormatter);
        String index = "history_log_" + yyMM;

        long kafkaResult = countSearchHits(index, buildSearchQuery(serialNumber, "PUBLISH TO KAFKA"));
        long logstashResult = countSearchHits(index, buildSearchQuery(serialNumber, "SAVE FROM LOGSTASH"));

        if (kafkaResult < 1 || logstashResult < 1) {
            return false;
        }

        return kafkaResult == logstashResult;
    }

    // 검색 쿼리 생성
    private QueryBuilder buildSearchQuery(String serialNumber, String stepTitle) {
        return QueryBuilders.boolQuery()
                .should(QueryBuilders.boolQuery()
                        .filter(QueryBuilders
                                .matchQuery("serial_number.keyword", serialNumber))
                        .filter(QueryBuilders
                                .matchQuery("step_title.keyword", stepTitle))
                        .filter(QueryBuilders
                                .matchQuery("is_success.keyword", "T")));
    }

    // 작성한 쿼리로 검색 실행
    private long countSearchHits(String index, QueryBuilder query) throws IOException {
        SearchRequest request = new SearchRequest()
                .indices(index)
                .source(new SearchSourceBuilder()
                        .query(query)
                        .sort("time", SortOrder.DESC)
                        .from(0)
                        .size(10000)
                );

        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        log.debug("SEARCH COUNTS = {}", response.getHits().getHits().length);

        return response.getHits().getHits().length;
    }
}
