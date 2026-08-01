package br.com.semblefe.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;

@Configuration
@EnableAsync
public class AsyncConfiguration implements AsyncConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            AsyncConfiguration.class);

    @Bean("emailNotificationExecutor")
    Executor emailNotificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("email-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);

        executor.setRejectedExecutionHandler((task, rejectedExecutor) -> {
            if (rejectedExecutor.isShutdown()) {
                throw new RejectedExecutionException(
                        "O executor de notificações está encerrado.");
            }

            LOGGER.warn(
                    "A fila de notificações atingiu o limite; aplicando backpressure.");

            task.run();
        });

        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (exception, method, parameters) -> LOGGER.error(
                "Falha assíncrona não tratada na entrega de e-mail. Tipo: {}",
                exception.getClass().getSimpleName());
    }
}