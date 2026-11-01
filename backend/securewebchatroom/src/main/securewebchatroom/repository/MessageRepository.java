package com.example.securewebchatroom.repository;

import com.example.securewebchatroom.entity.Message;
import com.example.securewebchatroom.enums.MessageStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByRoomIdAndStatusInOrderBySentAtAsc(Long roomId, Collection<MessageStatus> statuses);
    Optional<Message> findFirstByRoomIdAndStatusOrderBySentAtDesc(Long roomId, MessageStatus status);
}
