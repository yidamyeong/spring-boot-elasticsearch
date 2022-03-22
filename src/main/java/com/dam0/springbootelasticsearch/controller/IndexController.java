package com.dam0.springbootelasticsearch.controller;

import com.dam0.springbootelasticsearch.dto.IndexDto;
import com.dam0.springbootelasticsearch.dto.Result;
import com.dam0.springbootelasticsearch.runner.DemoApplicationRunner;
import com.dam0.springbootelasticsearch.service.IndexService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class IndexController {

    private final IndexService indexService;
    private final DemoApplicationRunner demoApplicationRunner;

    public IndexController(IndexService indexService, DemoApplicationRunner demoApplicationRunner) {
        this.indexService = indexService;
        this.demoApplicationRunner = demoApplicationRunner;
    }

    @PostMapping(value = "/doc", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Result createDocument(@ModelAttribute IndexDto indexDto) throws JsonProcessingException {
        indexService.createDocumentAsync(indexDto);

        return new Result("SUCCESS");
    }

    @PostMapping(value = "/doc/async", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Result createDocumentAsync(@ModelAttribute IndexDto indexDto) throws JsonProcessingException {
        long duration = indexService.asyncTest(indexDto);
        return new Result("SUCCESS", duration);
    }

    @PostMapping(value = "/doc/sync", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Result createDocumentSync(@ModelAttribute IndexDto indexDto) throws IOException {
        long duration = indexService.syncTest(indexDto);
        return new Result("SUCCESS", duration);
    }

    @PostMapping(value = "/index", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Result createIndex() throws IOException {
        indexService.createIndexSync();
        return new Result("SUCCESS");
    }

    @GetMapping("/fieldList")
    public Result getFieldList() {
        return new Result("SUCCESS", demoApplicationRunner.getList());
    }
}
