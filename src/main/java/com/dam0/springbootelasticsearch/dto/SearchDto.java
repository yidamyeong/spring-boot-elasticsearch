package com.dam0.springbootelasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchDto {
    @NotBlank
    private String index;
    private String key = "*";
    @NotBlank
    private String value;
    @Min(1)
    private int page = 1;
    @Min(1)
    private int size = 10;
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])")
    private String from;
    @Pattern(regexp = "\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])")
    private String to;
    private String datefield = "time";
    private String remoteIp;

    public String getIndex() {
        // wildcard 검색 가능하도록
        return index.toLowerCase() + "*";
    }

    public QueryBuilder getQuery() {
        if ("key_log".equals(this.key)) {
            this.key = "key_log.keyword";
        }

        QueryStringQueryBuilder queryStringBuilder;
        if ("*".equals(this.key)) {
            queryStringBuilder = QueryBuilders.queryStringQuery(this.value).defaultField(this.key);
        } else {
            queryStringBuilder = QueryBuilders.queryStringQuery(this.value).field(this.key);
        }

        if (this.from != null && this.to != null) {
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(queryStringBuilder);
            boolQueryBuilder.must(
                    rangeQuery(this.datefield)
                            .gte(this.from)
                            .lte(this.to)
            );
            return boolQueryBuilder;
        }

        return queryStringBuilder;
    }

}
