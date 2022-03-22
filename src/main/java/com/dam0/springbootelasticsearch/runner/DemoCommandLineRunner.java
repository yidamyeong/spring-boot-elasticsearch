package com.dam0.springbootelasticsearch.runner;

import com.dam0.springbootelasticsearch.api.IndexAPIs;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Order(1)
@Component
public class DemoCommandLineRunner implements CommandLineRunner {

    private final IndexAPIs indexAPIs;

    public DemoCommandLineRunner(IndexAPIs indexAPIs) {
        this.indexAPIs = indexAPIs;
    }

    @Override
    public void run(String... args) {
        Map<String, Object> source = new HashMap<>();
        source.put("@timestamp", new DateTime(DateTimeZone.UTC).toString());
        source.put("field1", "sample-value");
        source.put("field2", "dummy");

        indexAPIs.indexAsync("sample-index", source);
    }
}
