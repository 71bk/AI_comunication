package tw.bk.ai.service.usage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.bk.ai.entity.UsageLog;
import tw.bk.ai.repository.ChatRepository;
import tw.bk.ai.repository.MessageRepository;
import tw.bk.ai.repository.UsageLogRepository;
import tw.bk.ai.repository.UserRepository;

/**
 * Usage log service.
 */
@Service
@RequiredArgsConstructor
public class UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public void logUsage(Long userId, Long chatId, Long messageId, String provider, String model,
            Integer tokenIn, Integer tokenOut) {
        UsageLog log = UsageLog.builder()
                .user(userRepository.getReferenceById(userId))
                .chat(chatRepository.getReferenceById(chatId))
                .message(messageRepository.getReferenceById(messageId))
                .provider(provider)
                .model(model)
                .tokenIn(tokenIn != null ? tokenIn : 0)
                .tokenOut(tokenOut != null ? tokenOut : 0)
                .build();

        usageLogRepository.save(log);
    }
}
