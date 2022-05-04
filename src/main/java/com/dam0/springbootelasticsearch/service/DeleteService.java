package com.dam0.springbootelasticsearch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteService {

    private final RestHighLevelClient restHighLevelClient;

    // 2개 이상의 PK 기준으로 삭제 쿼리 생성 - QueryBuilder 사용
    public BulkByScrollResponse deleteByQuery(String index, List<String> pkList, List<Map<String, Object>> dataList,
                                               List<String> stringList) throws IOException {

        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();

        for (Map<String, Object> sourceMap : dataList) {
            Map<String, Object> pkMap = getPkMap(sourceMap, pkList);

            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            for (String key : pkMap.keySet()) {
                StringBuilder field = new StringBuilder();
                field.append(key);
                if (Objects.nonNull(stringList) && stringList.contains(key)) {
                    field.append(".keyword");
                }
                boolQuery.filter(QueryBuilders
                        .matchQuery(field.toString(), pkMap.get(key))
                        .operator(Operator.AND)
                );
            }
            queryBuilder.should(boolQuery);
        }

        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(queryBuilder)
                .setRefresh(true)
                .setConflicts("proceed");
        log.debug("DeleteByQueryRequest Source = {}", request.getSearchRequest().source());

        BulkByScrollResponse bulkResponse = restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        log.info("Total Docs Processed : {}", bulkResponse.getTotal());
        log.info("Total Docs Deleted : {}", bulkResponse.getDeleted());

        return bulkResponse;
    }

    // 2개 이상의 PK 기준으로 삭제 쿼리 생성
    private String buildDeleteQuery(List<String> pkList, List<Map<String, Object>> dataList, List<String> stringList) {
        StringBuilder query = new StringBuilder();
        query.append("{");
        query.append("\"query\": {");
        query.append("\"bool\": {");
        query.append("\"should\": [");

        boolean isFirstBool = true;
        for (Map<String, Object> sourceMap : dataList) {
            Map<String, Object> pkMap = getPkMap(sourceMap, pkList);
            if (isFirstBool) {
                isFirstBool = false;
                query.append("{");
            } else {
                query.append(", {");
            }
            query.append("\"bool\": {");
            query.append("\"filter\": [");

            boolean isFirstMatch = true;
            for (String key : pkMap.keySet()) {
                String field = key;
                if (Objects.nonNull(stringList) && stringList.contains(key)) {
                    field += ".keyword";
                }
                if (isFirstMatch) {
                    isFirstMatch = false;
                    query.append("{\"match\": {\"").append(field).append("\": \"").append(pkMap.get(key)).append("\"}}");
                } else {
                    query.append(", {\"match\": {\"").append(field).append("\": \"").append(pkMap.get(key)).append("\"}}");
                }
            }
            query.append("]}}");
        }
        query.append("]}}}");
        log.debug("# deleteQuery = {}", query);

        return query.toString();
    }

    // (PK, Value)로 이루어진 Map 반환
    private Map<String, Object> getPkMap(Map<String, Object> sourceMap, List<String> pkList) {
        Map<String, Object> pkMap = new HashMap<>();
        for (String pk : pkList) {
            if (Objects.nonNull(sourceMap.get(pk))) {
                pkMap.put(pk, sourceMap.get(pk));
            } else {
                throw new InvalidParameterException("A PARAMETER(" + pk + ") IS NOT VALID");
            }
        }
        log.debug("# pkMap = {}", pkMap);

        return pkMap;
    }

}
