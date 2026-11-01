package com.example.securewebchatroom.repository;

import com.example.securewebchatroom.entity.RoomMembership;
import com.example.securewebchatroom.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomMembershipRepository extends JpaRepository<RoomMembership, Long> {
    List<RoomMembership> findByUserId(Long userId);
    List<RoomMembership> findByRoomId(Long roomId);
    Optional<RoomMembership> findByRoomIdAndUserId(Long roomId, Long userId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
    long countByRoomId(Long roomId);
    long countByUserIdAndRole(Long userId, Role role);
}
