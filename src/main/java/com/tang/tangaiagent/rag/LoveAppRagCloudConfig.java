package com.tang.tangaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class LoveAppRagCloudConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApi;

    @Bean
    public Advisor loveAppRagCloudAdvisor() {
        //设置api_key
        DashScopeApi dashScopeApi = DashScopeApi.builder().apiKey(dashscopeApi).build();
        final String KNOWLEDGE_INDEX = "恋爱大师";
        //DashScopeDocumentRetrieverOptions负责配置
        //DashScopeDocumentRetriever(DocumentRetriever)负责执行，是检索器
        DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                DashScopeDocumentRetrieverOptions.builder()
                .indexName(KNOWLEDGE_INDEX).build()
        );
        //RetrievalAugmentationAdvisor是插件，负责拦截调用
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .build();
    }
}
