package tw.bk.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tw.bk.ai.dto.chat.ChatCreateReq;
import tw.bk.ai.dto.chat.ChatDetailResp;
import tw.bk.ai.dto.chat.ChatResp;
import tw.bk.ai.dto.chat.ChatTitleUpdateReq;
import tw.bk.ai.dto.chat.MessageSendReq;
import tw.bk.ai.result.Result;
import tw.bk.ai.security.JwtUserPrincipal;
import tw.bk.ai.service.chat.ChatService;
import tw.bk.ai.service.llm.LlmService;

import java.util.List;

/**
 * 對話控制器
 */
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final LlmService llmService;

    /**
     * 建立新對話
     * POST /api/chats
     */
    @PostMapping
    public ResponseEntity<Result<ChatResp>> createChat(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @Valid @RequestBody(required = false) ChatCreateReq req) {

        if (req == null) {
            req = new ChatCreateReq();
        }

        ChatResp resp = chatService.createChat(principal.getId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Result.ok(resp));
    }

    /**
     * 取得對話列表
     * GET /api/chats
     */
    @GetMapping
    public ResponseEntity<Result<List<ChatResp>>> getChatList(
            @AuthenticationPrincipal JwtUserPrincipal principal) {

        List<ChatResp> chats = chatService.getChatList(principal.getId());
        return ResponseEntity.ok(Result.ok(chats));
    }

    /**
     * 取得對話詳情（含訊息）
     * GET /api/chats/{chatId}
     */
    @GetMapping("/{chatId}")
    public ResponseEntity<Result<ChatDetailResp>> getChatDetail(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long chatId) {

        ChatDetailResp resp = chatService.getChatDetail(principal.getId(), chatId);
        return ResponseEntity.ok(Result.ok(resp));
    }

    /**
     * 刪除對話
     * DELETE /api/chats/{chatId}
     */
    @DeleteMapping("/{chatId}")
    public ResponseEntity<Result<Void>> deleteChat(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long chatId) {

        chatService.deleteChat(principal.getId(), chatId);
        return ResponseEntity.ok(Result.ok());
    }

    /**
     * 更新對話標題
     * PUT /api/chats/{chatId}/title
     */
    @PutMapping("/{chatId}/title")
    public ResponseEntity<Result<ChatResp>> updateChatTitle(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long chatId,
            @Valid @RequestBody ChatTitleUpdateReq req) {

        ChatResp resp = chatService.updateChatTitle(principal.getId(), chatId, req.getTitle());
        return ResponseEntity.ok(Result.ok(resp));
    }

    /**
     * 發送訊息並串流回覆
     * POST /api/chats/{chatId}/messages:stream
     */
    @PostMapping(value = "/{chatId}/messages:stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamMessage(
            @AuthenticationPrincipal JwtUserPrincipal principal,
            @PathVariable Long chatId,
            @Valid @RequestBody MessageSendReq req) {

        return llmService.streamChat(principal.getId(), chatId, req);
    }
}
