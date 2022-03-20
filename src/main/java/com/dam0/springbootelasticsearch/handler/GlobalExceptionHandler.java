package com.dam0.springbootelasticsearch.handler;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import com.dam0.springbootelasticsearch.dto.Error;
import com.dam0.springbootelasticsearch.dto.Result;
import com.dam0.springbootelasticsearch.util.HttpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Error Handler
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private final HttpServletRequest httpServletRequest;
    private final IndexAPIs indexAPIs;

    @Autowired
    public GlobalExceptionHandler(HttpServletRequest httpServletRequest, IndexAPIs indexAPIs) {
        this.httpServletRequest = httpServletRequest;
        this.indexAPIs = indexAPIs;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(JsonProcessingException.class)
    public Error handleJsonProcessingException(JsonProcessingException e) {
        LOGGER.error("## JsonProcessingException Occurred");
        saveFailLog(e.getMessage());
        LOGGER.error("JSON processing error", e);
        return new Error(400, "JSON convert error", e.getMessage());
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(IOException.class)
    public Result handleIOException(IOException e) {
        LOGGER.error("## IOException Occurred");
        saveFailLog(e.getMessage());
        LOGGER.error("Elasticsearch Exception", e);
        return new Result(503, "ElasticSearch timeout error", e.getMessage());
    }

    private Map<String, Object> getRequestSource() {
        Map<String, Object> requestSource = new HashMap<>();
        requestSource.put("index", httpServletRequest.getParameter("index"));
        requestSource.put("data", httpServletRequest.getParameter("data"));
        requestSource.put("remote_ip", HttpUtil.getRemoteIp(httpServletRequest));
        LOGGER.error("## Request source = {}", requestSource);

        return requestSource;
    }

    /**
     * Fail Data Handling
     */
    private void saveFailLog(String exceptionMessage) {
        Map<String, Object> fail = getRequestSource();
        fail.put("reason", exceptionMessage);
        fail.put("timestamp", new DateTime().toString());
        LOGGER.debug("Fail Data = {}", fail);

        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM");
        String index = "fail_" + DateTime.now().toString(dateTimeFormatter);

        indexAPIs.indexAsync(index, fail);
    }

}
