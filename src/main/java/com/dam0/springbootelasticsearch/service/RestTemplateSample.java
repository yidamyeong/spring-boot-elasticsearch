package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.dto.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestTemplateSample {

    private final RestTemplate restTemplate;
    @Value("${url.prefix}")
    private String urlPrefix;

    // application/x-www-form-urlencoded
    public ResponseEntity<Result> sendSerialNumber(String serialNumber) {
        URI uri = UriComponentsBuilder
                .fromUriString(urlPrefix)
                .path("/sample/serial-num")
                .encode()
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_FORM_URLENCODED.toString());
        headers.add("Accept", MediaType.APPLICATION_JSON.toString()); // 서버가 JSON 값 리턴하므로

        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("serial_num", serialNumber);

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Result> responseEntity =
                restTemplate.exchange(uri, HttpMethod.POST, httpEntity, Result.class);

        log.info("status code = {}", responseEntity.getStatusCode());

        return responseEntity;
    }

    // application/json
    public ResponseEntity<Result> sendSerialNumber2(String serialNumber) {
        URI uri = UriComponentsBuilder
                .fromUriString(urlPrefix)
                .path("/sample/serial-num")
                .encode()
                .build()
                .toUri();

        RequestEntity<String> requestEntity = RequestEntity
                .post(uri)
                .header("Content-Type", "application/json")
                .body(serialNumber);

        ResponseEntity<Result> responseEntity = restTemplate.exchange(requestEntity, Result.class);

        log.info("status code = {}", responseEntity.getStatusCode());

        return responseEntity;
    }

}
