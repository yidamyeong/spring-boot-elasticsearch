package com.dam0.springbootelasticsearch.controller;

import com.dam0.springbootelasticsearch.dto.IndexDto;
import com.dam0.springbootelasticsearch.dto.Result;
import com.dam0.springbootelasticsearch.service.IndexService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

    private final IndexService indexService;

    public IndexController(IndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping(value = "/doc", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Result createDocument(@ModelAttribute IndexDto indexDto) throws JsonProcessingException {
        indexService.createDocument(indexDto);

        return new Result("SUCCESS");
    }
}
