package com.example.securewebchatroom.repository;

import com.example.securewebchatroom.entity.ModerationSetting;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationSettingRepository extends JpaRepository<ModerationSetting, Long> {
    Optional<ModerationSetting> findByRoomId(Long roomId);
}
