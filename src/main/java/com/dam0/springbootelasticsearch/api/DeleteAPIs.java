package com.dam0.springbootelasticsearch.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAPIs {

    /**
     * Document Id 로 삭제하기
     */
    public static DocWriteResponse.Result deleteById(String index, String documentId,
                                                     RestHighLevelClient restHighLevelClient) throws IOException {
        DeleteRequest request = new DeleteRequest()
                .index(index)
                .id(documentId);

        DeleteResponse delete = restHighLevelClient.delete(request, RequestOptions.DEFAULT);  // IOException
        log.debug("delete result = {} ", delete);

        return delete.getResult();
    }

    // Document id 찾기
    public static String getDocId(String index, Map<String, Object> pkMap,
                            RestHighLevelClient restHighLevelClient) {
        List<String> pkList = new ArrayList<>(pkMap.keySet());

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        for (String pk : pkList) {
            queryBuilder.must(QueryBuilders.matchQuery(pk, pkMap.get(pk)).operator(Operator.AND));
        }

        SearchRequest request = new SearchRequest()
                .indices(index)
                .source(new SearchSourceBuilder()
                        .query(queryBuilder)
                        .sort("time", SortOrder.DESC)
                        .from(0)
                        .size(1)
                );

        SearchResponse response;
        try {
            response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
            log.debug("# SearchResponse OBJECT = {}", response);

            SearchHits hits = response.getHits();
            SearchHit[] searchHits = hits.getHits();
            if (searchHits.length > 0) {
                return searchHits[0].getId();
            }
        } catch (Exception e) {
            log.error("# Error occurred while searching documents before deleting [{}]", e.getMessage());
        }

        return null; // 조회결과가 없으면 null 반환
    }

    // Delete By Query
    public static BulkByScrollResponse deleteByQuery(String index, QueryBuilder query,
                                                     String dateField, int size,
                                                     RestHighLevelClient restHighLevelClient) throws IOException {
        DeleteByQueryRequest request = new DeleteByQueryRequest();

        // DeleteByQueryRequest 내부의 searchRequest 에서 from 값 주면 에러남
        request.getSearchRequest().indices(index)
                .source(new SearchSourceBuilder()
                        .query(query)
                        .sort(dateField, SortOrder.DESC)
                        .size(size)
                );

        // SearchRequest, DeleteByQueryRequest 각각 사이즈 설정 필수
        request.setSize(size);  // 6.8.0 까지는 사용 확인, 그 후로 Deprecated 된 듯
        log.debug("DeleteByQueryRequest : \n" + request.getSearchRequest().source());

        return restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
    }

}
