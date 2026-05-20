package com.tang.tangaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LoveAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    //加载所有的markdown本地文件
    public List<Document> loadMarkdownDocuments() {
        //存所有文件
        List<Document> allDocuments = new ArrayList<>();
        try {
            //设置读取本地文件的位置
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            //遍历读取
        for (Resource resource : resources) {
            //得到文件名
            String fileName = resource.getFilename();
            //使用MarkdownDocumentReaderConfig加载配置指定文件读取文档的细节，比如是否读取代码块等
            MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                    //是否开启水平线分割读取
                    .withHorizontalRuleCreateDocument(true)
                    .withIncludeCodeBlock(false)
                    .withIncludeBlockquote(false)
                    //加载元信息
                    .withAdditionalMetadata("filename", fileName)
                    .build();
            //正式按配置读取
            MarkdownDocumentReader reader = new MarkdownDocumentReader(resource,config);
            allDocuments.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown文件加载失败",e);
        }
        return allDocuments;
    }
}
