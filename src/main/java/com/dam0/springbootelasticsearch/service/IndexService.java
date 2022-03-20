package com.dam0.springbootelasticsearch.service;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import com.dam0.springbootelasticsearch.dto.IndexDto;
import com.dam0.springbootelasticsearch.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Service
public class IndexService {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexService.class);
    private final TypeReference<HashMap<String, Object>> typeRefHashMap = new TypeReference<>() {};
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;
    private final IndexAPIs indexAPIs;

    public IndexService(HttpServletRequest httpServletRequest, ObjectMapper objectMapper, IndexAPIs indexAPIs) {
        this.httpServletRequest = httpServletRequest;
        this.objectMapper = objectMapper;
        this.indexAPIs = indexAPIs;
    }

    public void createDocument(IndexDto indexDto) throws JsonProcessingException {
        Map<String, Object> source = objectMapper.readValue(indexDto.getData(), typeRefHashMap);  // JsonProcessingException

        if (!source.containsKey("time")) {
            source.put("@timestamp", generateTimestamp());
            source.put("time", generateTime());
        }
        source.put("remote_ip", HttpUtil.getRemoteIp(httpServletRequest));

        indexAPIs.indexAsync(indexDto.getIndex(), source);
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
