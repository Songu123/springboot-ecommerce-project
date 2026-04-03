package com.son.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    @JsonIgnore
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String senderType; // "CUSTOMER" or "ADMIN"

    // Can be "TEXT" or "IMAGE"
    @Column(length = 20)
    @Builder.Default
    private String messageType = "TEXT";

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // Text or Image URL

    @CreationTimestamp
    private LocalDateTime timestamp;
}
