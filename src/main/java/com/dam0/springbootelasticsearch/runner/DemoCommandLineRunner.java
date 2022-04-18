package com.dam0.springbootelasticsearch.runner;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.RestHighLevelClient;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Order(1)
@Component
@RequiredArgsConstructor
public class DemoCommandLineRunner implements CommandLineRunner {

    private final IndexAPIs indexAPIs;
    private final RestHighLevelClient restHighLevelClient;

    @Override
    public void run(String... args) {
        Map<String, Object> source = new HashMap<>();
        source.put("@timestamp", new DateTime(DateTimeZone.UTC).toString());
        source.put("field1", "sample-value");
        source.put("field2", "dummy");

        IndexAPIs.indexAsync("sample-index", source, restHighLevelClient);
    }
}
