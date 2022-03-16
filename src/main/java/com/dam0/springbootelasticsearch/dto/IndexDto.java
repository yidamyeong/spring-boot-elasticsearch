package com.dam0.springbootelasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexDto {

    private String index;
    private String data;

    public String addDateTimeOnIndex() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(index);
        stringBuilder.append("_");
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM");
        stringBuilder.append(DateTime.now().toString(dateTimeFormatter));

        return stringBuilder.toString();
    }
}
