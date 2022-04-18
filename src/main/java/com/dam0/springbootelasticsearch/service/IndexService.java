package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import com.dam0.springbootelasticsearch.dto.CT;
import com.dam0.springbootelasticsearch.dto.IndexDto;
import com.dam0.springbootelasticsearch.dto.Result;
import com.dam0.springbootelasticsearch.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.simple.JSONArray;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexService {

    private final TypeReference<HashMap<String, Object>> typeRefHashMap = new TypeReference<>() {};
    private final HttpServletRequest httpServletRequest;
    private final RestHighLevelClient restHighLevelClient;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final IndexAPIs indexAPIs;

    public long asyncTest(IndexDto indexDto) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            createDocumentAsync(indexDto);
        }
        long end = System.currentTimeMillis();

        return end - start;
    }

    public long syncTest(IndexDto indexDto) throws IOException {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            createDocumentSync(indexDto);
        }
        long end = System.currentTimeMillis();

        return end - start;
    }

    public void createDocumentAsync(IndexDto indexDto) throws JsonProcessingException {
        Map<String, Object> source = readValue(indexDto);

        IndexAPIs.indexAsync(indexDto.addDateTimeOnIndex(), source, restHighLevelClient);
    }

    private Map<String, Object> readValue(IndexDto indexDto) throws JsonProcessingException {
        Map<String, Object> source = objectMapper.readValue(indexDto.getData(), typeRefHashMap);  // JsonProcessingException

        if (!source.containsKey("time")) {
            source.put("@timestamp", generateTimestamp());
            source.put("time", generateTime());
        }
        source.put("remote_ip", HttpUtil.getRemoteIp(httpServletRequest));

        return source;
    }

    public void createDocumentSync(IndexDto indexDto) throws IOException {
        Map<String, Object> source = readValue(indexDto);

        IndexAPIs.index(indexDto.addDateTimeOnIndex(), source, restHighLevelClient);
    }

    public void createIndexSync() throws IOException {

        CreateIndexRequest request = new CreateIndexRequest("sample-config");

        request.settings(Settings.builder()
                .put("index.number_of_shards", 2)
                .put("index.number_of_replicas", 2)
        );

        // request mapping - (1)
        /*
        request.mapping(
                "{\n" +
                        "  \"properties\": {\n" +
                        "    \"category\": {\n" +
                        "      \"type\": \"text\"\n" +
                        "    }\n" +
                        "  }\n" +
                        "}",
                XContentType.JSON);
         */

        // request mapping - (2)
        Map<String, Object> category = new HashMap<>();
        category.put("type", "text");
        Map<String, Object> prefix = new HashMap<>();
        prefix.put("type", "text");

        Map<String, Object> properties = new HashMap<>();
        properties.put("category", category);
        properties.put("prefix", prefix);

        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        request.mapping(mapping);

//        request.alias(new Alias("index_alias").filter(QueryBuilders.termQuery("user", "dam0")));

        IndexAPIs.createIndexSync(request, restHighLevelClient);  // IOException
    }

    // now date
    private DateTime generateTime() {
        return new DateTime();
    }

    // UTC time
    private DateTime generateTimestamp() {
        return new DateTime(DateTimeZone.UTC);
    }

    public Result updateFinalIndex(String serialNumber) throws IOException {

        // serialNumber 에서 index 추출
        List<String> list = Arrays.stream(serialNumber.split("__")).collect(Collectors.toList());
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("_yyyy");
        String index = list.get(1);

        // index 에서 main, sub, type 추출
        List<String> mainSub = Arrays.stream(index.split("_")).collect(Collectors.toList());
        String type;
        if (mainSub.size() > 3) {
            type = mainSub.get(list.size() + 1);
        } else {
            type = mainSub.get(list.size());
        }
        String main = mainSub.get(0);
        StringBuilder sub = new StringBuilder();
        boolean isFirst = true;
        for (int i = 1; i < mainSub.size() - 1; i++) {
            if (isFirst) {
                sub.append(mainSub.get(i));
                isFirst = false;
            } else {
                sub.append("_").append(mainSub.get(i));
            }
        }

        // match 쿼리로 SearchRequest 생성
        QueryBuilder query = QueryBuilders.matchQuery("serial_number", serialNumber);
        SearchRequest request = new SearchRequest()
                .indices(index + DateTime.now().toString(dateTimeFormatter))
                .source(new SearchSourceBuilder()
                        .query(query)
                        .sort("time", SortOrder.DESC)
                        .from(0)
                        .size(10000)
                );

        // 검색 후
        TypeReference<?> typeRef = findExactEntity(index);
        SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        List<Object> entityList = Arrays.stream(response.getHits().getHits())
                .map(SearchHit::getSourceAsMap)
                .map(a -> objectMapper.convertValue(a, typeRef))
                .map(a -> objectMapper.convertValue(a, typeRefHashMap))
                .collect(Collectors.toList());

        JSONArray array = new JSONArray();
        array.addAll(entityList);

        String data = array.toJSONString();
        log.debug(">>>>> data = {}", data);
        log.debug(">>>>> type = {}, main = {}, sub = {}", type, main, sub);

        return callDoccEtlApi(type, main, sub.toString(), data);
    }

    // application/x-www-form-urlencoded
    public Result callDoccEtlApi(String type, String main, String sub, String data) {
        URI uri = UriComponentsBuilder
                .fromUriString("url-sample")
                .path("/sample/load/" + type)
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
        headers.add("Accept", MediaType.APPLICATION_JSON.toString()); // 서버가 JSON 값 리턴하므로

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("main", main);
        requestBody.add("sub", sub);
        requestBody.add("data", data);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Result> responseEntity =
                restTemplate.exchange(uri, HttpMethod.POST, httpEntity, Result.class);

        log.info("status code = {}", responseEntity.getStatusCode());

        return responseEntity.getBody();
    }


    private TypeReference<?> findExactEntity(String index) {

        switch (index) {
            case "ct_waiting_24h":
                return new TypeReference<CT.Waiting24h>() {};
            case "ct_reservation_24h":
                return new TypeReference<CT.Reservation24h>() {};
            case "ct_unreserved_24h":
                return new TypeReference<CT.Unreserved24h>() {};
            case "ct_prescription_past_24h":
                return new TypeReference<CT.PrescriptionPast24h>() {};
            case "ct_prescription_predict_24h":
                return new TypeReference<CT.PrescriptionPredict24h>() {};
            case "ct_progress_24h":
                return new TypeReference<CT.Progress24h>() {};
            case "ct_progress_1m":
                return new TypeReference<CT.Progress1m>() {};
            default:
                throw new IllegalStateException("Unexpected value(index): " + index);
        }
    }

}
