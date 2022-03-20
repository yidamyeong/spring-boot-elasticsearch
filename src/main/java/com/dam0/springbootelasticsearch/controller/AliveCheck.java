package com.dam0.springbootelasticsearch.controller;

import com.dam0.springbootelasticsearch.dto.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AliveCheck {

    /**
     * health check API
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Result checkAlive() {
        return new Result("ALIVE");
    }
}
