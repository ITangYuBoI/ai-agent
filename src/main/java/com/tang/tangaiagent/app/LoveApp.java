package com.tang.tangaiagent.app;

import com.tang.tangaiagent.advisor.MyLoggerAdvisor;
import com.tang.tangaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static com.alibaba.dashscope.app.AppKeywords.TOP_K;
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Component
@Slf4j
public class LoveApp {

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。" +
            "开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；恋爱状态询问沟通、习惯差异引发的矛盾；" +
            "已婚状态询问家庭责任与亲属关系处理的问题。引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。\n";

    public LoveApp(ChatModel dashScopeChatModel) {
        //创建存储层
        InMemoryChatMemoryRepository chatMemoryRepository = new InMemoryChatMemoryRepository();

//        String fileDir = System.getProperty("user.dir")+"/tem/chat-memory";
//        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);

        //初始化管理层chatMemory
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        //选择chatClient的模型,并且设置默认advisor
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultSystem(SYSTEM_PROMPT+"对话后要生成恋爱结果报告，标题为 {用户} 的恋爱报告，内容为建议列表")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                        //自定义拦截器
                        ,new MyLoggerAdvisor()
                        )
                .build();
    }

    //ai 基础对话，支持多轮对话
    public String doChat(String message,String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId)
                        .param(TOP_K, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }

    record LoveReport(String title, List<String> suggestion){}

    //结构化输出
    public LoveReport doChatWithReport(String message,String chatId) {
        //快速生成一个类-java21
        LoveReport loveReport = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId)
                        .param(TOP_K, 10))
                .call()
                .entity(LoveReport.class);

        log.info("loveReport: {}",loveReport);
        System.out.println(loveReport);
        return loveReport;
    }

    //基于rag的对话增强
    @Resource
    private VectorStore loveAppVectorStore;

    @Resource(name = "pgVectorVectorStore")
    private VectorStore pgVectorStore;
    //基于云知识库
    @Resource
    private Advisor loloveAppRagCloudAdvisor;

    //引入查询重写器
    @Resource
    private QueryRewriter queryRewriter;

    public String doChatWithRag(String message,String chatId) {
        //查询重写用户prompt
        String rewrite = queryRewriter.doQueryRewrite(message);
        //快速生成一个类-java21
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(rewrite)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId)
                        .param(TOP_K, 10))
                .advisors(
//                      QuestionAnswerAdvisor.builder(loveAppVectorStore).build()
                        //基于云知识库
                        loloveAppRagCloudAdvisor
                        //基于pgVector向量存储
//                        QuestionAnswerAdvisor.builder(pgVectorStore).build()
                )
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}",content);
        return content;
    }


    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId)
                        .param(TOP_K, 10))
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMcp(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId)
                        .param(TOP_K, 10))
                // 开启日志，便于观察效果
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    //流式输出
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CONVERSATION_ID, chatId)
                        .param(TOP_K, 10))
                .stream()
                .content();
    }

}
