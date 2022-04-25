package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.dto.Result;
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
    public Result callElasticApi(String method, String endpoint, String queryString) {

        try (RestClient restClient = RestClient.builder(new HttpHost(host, port, "http")).build()) {
            log.debug("# ElasticService >>> host = {}, port = {}", host, port);

            HttpEntity entity = new NStringEntity(queryString, ContentType.APPLICATION_JSON);
            Request request = new Request(method, endpoint);
            request.addParameter("pretty", "true");
            request.setEntity(entity);

            Response response = restClient.performRequest(request);
            log.debug("# response = {}", response);

            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            return new Result(statusCode, "SUCCESS", responseBody);
        } catch (Exception e) {
            return new Result(500, e.getMessage(), e);
        }
    }

    public Result sendPost(String builtIndex, String action, String queryString) {
        String endpoint = "/" + builtIndex + "/" + action;
        log.debug("endpoint = {}", endpoint);
        Result result = callElasticApi("POST", endpoint, queryString);
        log.debug("result = {}", result);

        return result;
    }

    public Result sendGet(String builtIndex, String action, String queryString) {
        String endpoint = "/" + builtIndex + "/" + action;
        log.debug("endpoint = {}", endpoint);
        Result result = callElasticApi("GET", endpoint, queryString);
        log.debug("result = {}", result);

        return result;
    }

}
