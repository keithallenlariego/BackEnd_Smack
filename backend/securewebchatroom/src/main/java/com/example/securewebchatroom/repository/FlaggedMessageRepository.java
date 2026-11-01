package com.example.securewebchatroom.repository;

import com.example.securewebchatroom.entity.FlaggedMessage;
import com.example.securewebchatroom.enums.ReviewStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlaggedMessageRepository extends JpaRepository<FlaggedMessage, Long> {
    // "MessageRoomId" = message.room.id, "MessageRoomOwnerId" = message.room.owner.id
    List<FlaggedMessage> findByMessageRoomIdOrderByFlaggedAtDesc(Long roomId);
    long countByMessageRoomOwnerIdAndReviewStatus(Long ownerId, ReviewStatus reviewStatus);
}
