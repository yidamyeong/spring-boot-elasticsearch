package com.dam0.springbootelasticsearch.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteAPIs {

    private final RestHighLevelClient restHighLevelClient;

    /**
     * Document Id 로 삭제하기
     */
    public DocWriteResponse.Result deleteById(String index, String documentId) throws IOException {
        DeleteRequest request = new DeleteRequest()
                .index(index)
                .id(documentId);

        DeleteResponse delete = restHighLevelClient.delete(request, RequestOptions.DEFAULT);  // IOException
        log.debug("delete result = {} ", delete);

        return delete.getResult();
    }

}
