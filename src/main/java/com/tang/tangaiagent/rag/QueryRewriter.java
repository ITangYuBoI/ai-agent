package com.tang.tangaiagent.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.stereotype.Component;

@Component
public class QueryRewriter {

    private final QueryTransformer queryTransformer;

    //注入实现queryTransformer的Bean
    public QueryRewriter(ChatModel dashscopChatModel) {
        ChatClient.Builder builder = ChatClient.builder(dashscopChatModel);
        queryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }
    /**
     * 执行查询重写
     */
    public String doQueryRewrite(String prompt) {
        Query query = new Query(prompt);
        //执行重写
        Query transform = queryTransformer.transform(query);
        //输出重写内容
        return transform.text();
    }
}
