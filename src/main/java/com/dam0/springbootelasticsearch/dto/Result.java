package com.dam0.springbootelasticsearch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result {

    private int resultCode;
    private String resultMsg;
    private Object resultData;

    public Result() {
        this.setResultCode(200);
    }

    public Result(String resultMsg) {
        setResultCode(200);
        setResultMsg(resultMsg);
        setResultData(null);
    }

    public Result(String resultMsg, Object resultData) {
        setResultCode(200);
        setResultMsg(resultMsg);
        setResultData(resultData);
    }
}
