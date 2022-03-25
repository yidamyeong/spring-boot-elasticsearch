package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import com.dam0.springbootelasticsearch.dto.IndexDto;
import com.dam0.springbootelasticsearch.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class IndexService {

    private final TypeReference<HashMap<String, Object>> typeRefHashMap = new TypeReference<>() {};
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;
    private final IndexAPIs indexAPIs;

    public IndexService(HttpServletRequest httpServletRequest, ObjectMapper objectMapper, IndexAPIs indexAPIs) {
        this.httpServletRequest = httpServletRequest;
        this.objectMapper = objectMapper;
        this.indexAPIs = indexAPIs;
    }

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

        indexAPIs.indexAsync(indexDto.addDateTimeOnIndex(), source);
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

        indexAPIs.index(indexDto.addDateTimeOnIndex(), source);
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

        indexAPIs.createIndexSync(request);  // IOException
    }

    // now date
    private DateTime generateTime() {
        return new DateTime();
    }

    // UTC time
    private DateTime generateTimestamp() {
        return new DateTime(DateTimeZone.UTC);
    }

}
