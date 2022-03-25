package com.dam0.springbootelasticsearch.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ObjectUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonUtil {

    private static final TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // JSONArrayString -> List<Map>
    public static List<Map<String, Object>> getListFromJsonArray(String jsonArrayString) throws JsonProcessingException {

        JSONArray jsonArray = objectMapper.readValue(jsonArrayString, JSONArray.class);  // JsonProcessingException

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object obj : jsonArray) {
            list.add(objectMapper.convertValue(obj, typeRef));
        }

        return list;
    }

    // JSONArray -> List<Map>
    public static List<Map<String, Object>> getListFromJsonArray(JSONArray jsonArray) {
        if (ObjectUtils.isEmpty(jsonArray)) {
            log.error("# JsonUtil.getListFromJsonArray() >> PARAMETER(jsonArray) IS NULL");
            throw new IllegalArgumentException("A PARAMETER(jsonArray) IS NULL");
        }

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object jsonObject : jsonArray) {
            list.add(getMapFromJSONObject((JSONObject) jsonObject));
        }

        return list;
    }

    // JSON File -> List<Map>
    public static List<Map<String, Object>> getListFromJsonFile(String jsonFileName) throws Exception {
        String filePath = "json/";
        ClassPathResource resource = new ClassPathResource(filePath + jsonFileName);

        JSONArray jsonArray = (JSONArray) new JSONParser()
                .parse(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

        return getListFromJsonArray(jsonArray);
    }

    // JSONObject -> Map<String, Object>
    public static Map<String, Object> getMapFromJSONObject(JSONObject jsonObject) {
        if (ObjectUtils.isEmpty(jsonObject)) {
            log.error("# JsonUtil.getMapFromJSONObject() >> PARAMETER(jsonObject) IS NULL");
            throw new IllegalArgumentException("A PARAMETER(jsonObject) IS NULL");
        }

        try {
            return objectMapper.readValue(jsonObject.toJSONString(), typeRef);
        } catch (Exception e) {
            log.error("# JsonUtil.getMapFromJSONObject() >> JSONObject Reading as Map Failed");
            throw new RuntimeException(e);
        }
    }

}
