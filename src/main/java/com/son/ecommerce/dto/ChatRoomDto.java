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
public class ChatRoomDto {
    private Long id;
    private String sessionId;
    private String userName;
    private String lastMessage;
    private LocalDateTime lastUpdate;
    private boolean closed;
}
