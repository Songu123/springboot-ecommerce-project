package com.son.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long id;
    private String roomId; // sessionId
    private String senderType; // CUSTOMER or ADMIN
    private String senderName; // E.g., 'User123' or 'Khách vãng lai'
    private String messageType; // TEXT or IMAGE
    private String content;
    private LocalDateTime timestamp;
}
