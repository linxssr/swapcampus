package com.campus.chat.controller;

import com.campus.chat.service.ChatService;
import com.campus.common.result.Result;
import com.campus.common.utils.JwtUtil;
import com.campus.model.vo.ChatConversationVO;
import com.campus.model.vo.ChatMessageVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
public class ChatController {

    private final ChatService chatService;

    private final JwtUtil jwtUtil;

    public ChatController(ChatService chatService, JwtUtil jwtUtil) {
        this.chatService = chatService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> conversations(HttpServletRequest request) {
        Long uid = parseCurrentUid(request);
        return Result.success(chatService.getConversations(uid));
    }

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(chatService.uploadImage(file));
    }

    @GetMapping("/history/{toUid}")
    public Result<List<ChatMessageVO>> history(@PathVariable Long toUid, HttpServletRequest request) {
        Long fromUid = parseCurrentUid(request);
        return Result.success(chatService.getHistory(fromUid, toUid));
    }

    @PutMapping("/read/{msgId}")
    public Result<Void> markRead(@PathVariable Long msgId, HttpServletRequest request) {
        Long currentUid = parseCurrentUid(request);
        chatService.markMessageRead(currentUid, msgId);
        return Result.success();
    }

    private Long parseCurrentUid(HttpServletRequest request) {
        Long currentUid = jwtUtil.parseUserId(request.getHeader("Authorization"));
        if (currentUid == null) {
            currentUid = jwtUtil.parseUserId(request.getParameter("uid"));
        }
        return currentUid;
    }
}
