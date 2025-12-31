package tw.bk.ai.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.bk.ai.dto.chat.MessageResp;
import tw.bk.ai.entity.Chat;
import tw.bk.ai.entity.Message;
import tw.bk.ai.exception.NotFoundException;
import tw.bk.ai.repository.ChatRepository;
import tw.bk.ai.repository.MessageRepository;

import java.util.List;

/**
 * 訊息服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;

    /**
     * 保存使用者訊息
     */
    @Transactional
    public Message saveUserMessage(Long userId, Long chatId, String content) {
        Chat chat = chatRepository.findByIdAndUser_Id(chatId, userId)
                .orElseThrow(() -> NotFoundException.chat(chatId));

        Message message = Message.builder()
                .chat(chat)
                .role(Message.MessageRole.user)
                .content(content)
                .build();

        message = messageRepository.save(message);
        log.debug("User message saved: {} in chat: {}", message.getId(), chatId);

        return message;
    }

    /**
     * 保存 AI 回覆訊息
     */
    @Transactional
    public Message saveAssistantMessage(Long chatId, String content, String provider, String model,
            Integer tokenIn, Integer tokenOut) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> NotFoundException.chat(chatId));

        Message message = Message.builder()
                .chat(chat)
                .role(Message.MessageRole.assistant)
                .content(content)
                .provider(provider)
                .model(model)
                .tokenIn(tokenIn)
                .tokenOut(tokenOut)
                .build();

        message = messageRepository.save(message);
        log.debug("Assistant message saved: {} in chat: {}", message.getId(), chatId);

        return message;
    }

    /**
     * 取得對話的所有訊息
     */
    @Transactional(readOnly = true)
    public List<Message> getMessages(Long chatId) {
        return messageRepository.findByChat_IdOrderByCreatedAtAsc(chatId);
    }

    /**
     * 取得對話的訊息回應列表
     */
    @Transactional(readOnly = true)
    public List<MessageResp> getMessageResponses(Long chatId) {
        return messageRepository.findByChat_IdOrderByCreatedAtAsc(chatId).stream()
                .map(MessageResp::from)
                .toList();
}
}
