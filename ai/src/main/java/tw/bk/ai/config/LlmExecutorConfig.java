package tw.bk.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Task executor for LLM streaming jobs.
 */
@Configuration
public class LlmExecutorConfig {

    @Bean(name = "llmTaskExecutor")
    public TaskExecutor llmTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("llm-stream-");
        executor.initialize();
        return executor;
    }
}
