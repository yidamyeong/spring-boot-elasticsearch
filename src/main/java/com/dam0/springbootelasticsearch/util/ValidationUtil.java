package com.dam0.springbootelasticsearch.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ValidationUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void rejectIfEmptyOrWhitespace(List<String> keyList, Map<String, Object> sourceMap) {
        Map<String, Object> paramMap = new HashMap<>();
        for (String key : keyList) {
            if (Objects.nonNull(sourceMap.get(key))) {
                paramMap.put(key, sourceMap.get(key));
            } else {
                throw new InvalidParameterException("A PARAMETER(" + key + ") DOES NOT EXIST");
            }
        }
        rejectIfEmptyOrWhitespace(paramMap);
    }

    public static void rejectIfEmptyOrWhitespace(Map<String, Object> paramMap) {
        for (String key : paramMap.keySet()) {
            String value = paramMap.get(key).toString();
            if (Objects.isNull(value)) {
                throw new InvalidParameterException("A PARAMETER(" + key + ") IS NULL");
            } else if (value.isEmpty()) {
                throw new InvalidParameterException("A PARAMETER(" + key + ") IS EMPTY");
            } else if (value.replaceAll(" ", "").isEmpty()) {
                throw new InvalidParameterException("A PARAMETER(" + key + ") IS FILLED WITH WHITESPACE");
            }
        }
    }

    public static void rejectIfDataTypeNotMatched(List<String> dateList, List<String> intList, List<String> doubleList,
                                                  TypeReference<?> typeRef, Map<String, Object> sourceMap) {

        if (Objects.nonNull(dateList)) {
            String pattern = "\\d{4}-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])";

            for (String key : dateList) {
                if (!sourceMap.containsKey(key)) {
                    throw new InvalidParameterException("A PARAMETER(" + key + ") IS NULL");
                }
                String date = sourceMap.get(key).toString();
                boolean regex = Pattern.matches(pattern, date);
                if (!regex) {
                    throw new InvalidParameterException("THE PATTERN OF PARAMETER(" + key + ": " + sourceMap.get(key) + ") IS NOT VALID");
                }
            }
        }

        if (Objects.nonNull(intList)) {
            String regex = "^[-]?\\d*";
            rejectIfRegexIsNotMatched(intList, sourceMap, regex);
        }

        if (Objects.nonNull(doubleList)) {
            String regex = "^(0)|^[^0]\\d*|^[^0]\\d*\\.\\d|^(0.)\\d";
            rejectIfRegexIsNotMatched(doubleList, sourceMap, regex);
        }

        Object obj = objectMapper.convertValue(sourceMap, typeRef);
        log.debug("obj = {}", obj);
    }

    private static void rejectIfRegexIsNotMatched(List<String> paramList, Map<String, Object> sourceMap, String regex) {
        Pattern pattern = Pattern.compile(regex);

        for (String key : paramList) {
            if (!sourceMap.containsKey(key)) {
                throw new InvalidParameterException("A PARAMETER(" + key + ") IS NULL");
            }
            String input = sourceMap.get(key).toString();
            Matcher matcher = pattern.matcher(input);
            if (!matcher.matches()) {
                throw new InvalidParameterException("DATA TYPE OF PARAMETER(" + key + ": " + sourceMap.get(key) + ") IS NOT MATCHED");
            }
        }
    }

    public static void rejectIfEmptyOrWhitespace(String parameter) {
        if (parameter == null || parameter.isEmpty() || parameter.replaceAll(" ", "").length() == 0) {
            throw new InvalidParameterException("PARAMETER IS NOT VALID");
        }
    }
}
