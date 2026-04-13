package com.driveshare.controller;

import com.driveshare.model.Message;
import com.driveshare.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController extends BaseController {

    @Autowired private MessageRepository messageRepository;

    /** Returns all messages involving the current user. */
    @GetMapping
    public ResponseEntity<List<Message>> listMessages(
            @RequestHeader(value = "X-Session-Token", required = false) String token) {
        Long userId = resolveUserId(token);
        List<Message> mine = messageRepository.findAll().stream()
                .filter(m -> userId.equals(m.getSenderId()) || userId.equals(m.getReceiverId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(mine);
    }

    @PostMapping
    public ResponseEntity<String> sendMessage(
            @RequestHeader(value = "X-Session-Token", required = false) String token,
            @RequestBody Map<String, Object> body) {
        Message msg = new Message();
        msg.setSenderId(resolveUserId(token));
        msg.setReceiverId(Long.parseLong(body.get("receiverId").toString()));
        msg.setContent(body.get("content").toString());
        messageRepository.save(msg);
        return ResponseEntity.ok("Message sent");
    }
}
