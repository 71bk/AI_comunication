package tw.bk.ai.service.chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.bk.ai.dto.chat.ChatCreateReq;
import tw.bk.ai.dto.chat.ChatDetailResp;
import tw.bk.ai.dto.chat.ChatResp;
import tw.bk.ai.entity.Chat;
import tw.bk.ai.entity.User;
import tw.bk.ai.exception.NotFoundException;
import tw.bk.ai.repository.ChatRepository;
import tw.bk.ai.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 對話服務
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;

    /**
     * 建立新對話
     */
    @Transactional
    public ChatResp createChat(Long userId, ChatCreateReq req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> NotFoundException.user(userId));

        Chat chat = Chat.builder()
                .user(user)
                .title(req.getTitle() != null ? req.getTitle() : "New Chat")
                .build();

        chat = chatRepository.save(chat);
        log.info("Chat created: {} for user: {}", chat.getId(), userId);

        return ChatResp.from(chat);
    }

    /**
     * 取得使用者的對話列表
     */
    @Transactional(readOnly = true)
    public List<ChatResp> getChatList(Long userId) {
        return chatRepository.findByUser_IdOrderByUpdatedAtDesc(userId).stream()
                .map(ChatResp::from)
                .collect(Collectors.toList());
    }

    /**
     * 取得對話詳情（含訊息）
     */
    @Transactional(readOnly = true)
    public ChatDetailResp getChatDetail(Long userId, Long chatId) {
        Chat chat = chatRepository.findByIdAndUserIdWithMessages(chatId, userId)
                .orElseThrow(() -> NotFoundException.chat(chatId));

        return ChatDetailResp.from(chat);
    }

    /**
     * 刪除對話
     */
    @Transactional
    public void deleteChat(Long userId, Long chatId) {
        Chat chat = chatRepository.findByIdAndUser_Id(chatId, userId)
                .orElseThrow(() -> NotFoundException.chat(chatId));

        chatRepository.delete(chat);
        log.info("Chat deleted: {} by user: {}", chatId, userId);
    }

    /**
     * 更新對話標題
     */
    @Transactional
    public ChatResp updateChatTitle(Long userId, Long chatId, String title) {
        Chat chat = chatRepository.findByIdAndUser_Id(chatId, userId)
                .orElseThrow(() -> NotFoundException.chat(chatId));

        chat.setTitle(title);
        chat = chatRepository.save(chat);

        return ChatResp.from(chat);
    }
}
