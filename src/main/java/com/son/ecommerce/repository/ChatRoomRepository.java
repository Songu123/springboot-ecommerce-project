package com.son.ecommerce.repository;

import com.son.ecommerce.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findBySessionId(String sessionId);

    // Lấy các room mới nhắn tin gần nhất, sắp xếp giảm dần theo ID hoặc last message
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"messages", "user"})
    @Query("SELECT r FROM ChatRoom r ORDER BY r.id DESC")
    List<ChatRoom> findAllRoomsOrderByLatest();
}
