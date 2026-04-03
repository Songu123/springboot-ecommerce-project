package com.son.ecommerce.controller.api;

import com.son.ecommerce.dto.ChatMessageDto;
import com.son.ecommerce.dto.ChatRoomDto;
import com.son.ecommerce.entity.ChatMessage;
import com.son.ecommerce.entity.ChatRoom;
import com.son.ecommerce.repository.ChatMessageRepository;
import com.son.ecommerce.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;

    // --- STOMP Endpoints ---

    /**
     * Nhận tin nhắn từ STOMP (client gửi tới /app/chat.send)
     */
    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessageDto chatMessageDto) {
        String roomId = chatMessageDto.getRoomId();
        
        // 1. Tìm hoặc tạo ChatRoom
        ChatRoom room = chatRoomRepository.findBySessionId(roomId).orElseGet(() -> {
            ChatRoom newRoom = ChatRoom.builder()
                    .sessionId(roomId)
                    .closed(false)
                    .build();
            return chatRoomRepository.save(newRoom);
        });

        // 2. Lưu tin nhắn
        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .senderType(chatMessageDto.getSenderType())
                .messageType(chatMessageDto.getMessageType() != null ? chatMessageDto.getMessageType() : "TEXT")
                .content(chatMessageDto.getContent())
                .build();
        message = chatMessageRepository.save(message);

        chatMessageDto.setId(message.getId());
        chatMessageDto.setTimestamp(message.getTimestamp());

        // 3. Broadcast tin nhắn tới những ai đang theo dõi phòng này
        messagingTemplate.convertAndSend("/topic/messages/" + roomId, chatMessageDto);

        // 4. Cập nhật tên vào Room nếu là CUSTOMER gửi (để admin dễ phân biệt)
        if ("CUSTOMER".equals(chatMessageDto.getSenderType())) {
            if (chatMessageDto.getSenderName() != null && !chatMessageDto.getSenderName().trim().isEmpty()) {
                room.setCustomerName(chatMessageDto.getSenderName());
                chatRoomRepository.save(room);
            } else if (room.getCustomerName() == null) {
                chatMessageDto.setSenderName("Khách vãng lai");
            } else {
                chatMessageDto.setSenderName(room.getCustomerName());
            }
            messagingTemplate.convertAndSend("/topic/admin/chats", chatMessageDto);
        }
    }

    // --- REST APIs ---

    @GetMapping("/api/chat/history/{roomId}")
    public ResponseEntity<List<ChatMessageDto>> getChatHistory(@PathVariable String roomId) {
        return chatRoomRepository.findBySessionId(roomId)
                .map(room -> {
                    List<ChatMessageDto> messages = chatMessageRepository.findByChatRoomIdOrderByTimestampAsc(room.getId())
                            .stream()
                            .map(m -> ChatMessageDto.builder()
                                    .id(m.getId())
                                    .roomId(roomId)
                                    .senderType(m.getSenderType())
                                    .senderName("CUSTOMER".equals(m.getSenderType()) && room.getUser() != null ? room.getUser().getFullName() : "")
                                    .messageType(m.getMessageType())
                                    .content(m.getContent())
                                    .timestamp(m.getTimestamp())
                                    .build())
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(messages);
                })
                .orElse(ResponseEntity.ok(List.of()));
    }

    @GetMapping("/api/admin/chat/rooms")
    public ResponseEntity<List<ChatRoomDto>> getAdminChatRooms() {
        List<ChatRoomDto> rooms = chatRoomRepository.findAllRoomsOrderByLatest().stream().map(r -> {
            String lastMsg = "";
            if (r.getMessages() != null && !r.getMessages().isEmpty()) {
                ChatMessage lastM = r.getMessages().get(r.getMessages().size() - 1);
                lastMsg = "IMAGE".equals(lastM.getMessageType()) ? "[Hình ảnh]" : lastM.getContent();
            }
            String displayUserName = r.getCustomerName();
            if (displayUserName == null || displayUserName.isEmpty()) {
                displayUserName = r.getUser() != null ? r.getUser().getFullName() : "Khách ẩn danh";
            }
            
            return ChatRoomDto.builder()
                    .id(r.getId())
                    .sessionId(r.getSessionId())
                    .userName(displayUserName)
                    .lastMessage(lastMsg)
                    .lastUpdate(r.getCreatedAt()) // Hoặc timestamp tin nhắn cuối
                    .closed(r.isClosed())
                    .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(rooms);
    }

    // --- UPLOAD ENDPOINT ---
    @PostMapping("/api/chat/upload")
    @ResponseBody
    public String uploadChatImage(@RequestParam("imageFile") MultipartFile file) {
        try {
            String uploadDir = "src/main/resources/static/uploads/chat/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")) : ".webp";
            String newFileName = UUID.randomUUID().toString() + extension;

            Path filePath = Paths.get(uploadDir + newFileName);
            Files.write(filePath, file.getBytes());

            return "/uploads/chat/" + newFileName;
        } catch (Exception e) {
            System.err.println("Error uploading chat image: " + e.getMessage());
            return "error";
        }
    }
}
