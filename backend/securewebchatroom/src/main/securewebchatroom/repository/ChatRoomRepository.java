package com.example.securewebchatroom.repository;

import com.example.securewebchatroom.entity.ChatRoom;
import com.example.securewebchatroom.enums.Visibility;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    List<ChatRoom> findByVisibility(Visibility visibility);
}
