package com.dam0.springbootelasticsearch.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticService {

    @Value("${elasticsearch.host}")
    private String host;

    @Value("${elasticsearch.port}")
    private int port;

    /**
     * 엘라스틱서치로 전송하는 API
     */
    public Map<String, Object> callElasticApi(String method, String endpoint, String queryString) {
        Map<String, Object> result = new HashMap<>();

        try (RestClient restClient = RestClient.builder(new HttpHost(host, port, "http")).build()) {
            log.debug("ElasticService >>> host = {}, port = {}", host, port);

            Request request = new Request(method, endpoint);
            request.addParameter("pretty", "true");

            HttpEntity entity = new NStringEntity(queryString, ContentType.APPLICATION_JSON);
            request.setEntity(entity);
            Response response = restClient.performRequest(request);

            log.debug("response = {}", response);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            result.put("resultCode", statusCode);
            result.put("resultData", responseBody);
        } catch (Exception e) {
            result.put("resultCode", 500);
            result.put("resultMsg", e.getMessage());
            result.put("resultData", e.toString());
        }

        return result;
    }

    public Map<String, Object> sendPost(String builtIndex, String action, String queryString) {
        String endpoint = "/" + builtIndex + "/" + action;
        log.debug("endpoint = {}", endpoint);
        Map<String, Object> resultMap = callElasticApi("POST", endpoint, queryString);
        log.debug("resultMap = {}", resultMap);

        return resultMap;
    }

    public Map<String, Object> sendGet(String builtIndex, String action, String queryString) {
        String endpoint = "/" + builtIndex + "/" + action;
        log.debug("endpoint = {}", endpoint);
        Map<String, Object> resultMap = callElasticApi("GET", endpoint, queryString);
        log.debug("resultMap = {}", resultMap);

        return resultMap;
    }

}
