package com.example.securewebchatroom.controller;

import com.example.securewebchatroom.dto.RoomDto.MemberResponse;
import com.example.securewebchatroom.dto.RoomDto.RoomRequest;
import com.example.securewebchatroom.dto.RoomDto.RoomResponse;
import com.example.securewebchatroom.service.MembershipService;
import com.example.securewebchatroom.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final MembershipService membershipService;

    public RoomController(RoomService roomService, MembershipService membershipService) {
        this.roomService = roomService;
        this.membershipService = membershipService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@AuthenticationPrincipal Long userId, @Valid @RequestBody RoomRequest request) {
        return roomService.create(userId, request);
    }

    // Rooms I belong to
    @GetMapping
    public List<RoomResponse> myRooms(@AuthenticationPrincipal Long userId) {
        return roomService.myRooms(userId);
    }

    // Public rooms I can join
    @GetMapping("/public")
    public List<RoomResponse> publicRooms(@AuthenticationPrincipal Long userId) {
        return roomService.publicRooms(userId);
    }

    @GetMapping("/{roomId}")
    public RoomResponse get(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        return roomService.get(roomId, userId);
    }

    @PutMapping("/{roomId}")
    public RoomResponse update(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId,
                               @Valid @RequestBody RoomRequest request) {
        return roomService.update(roomId, userId, request);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        roomService.delete(roomId, userId);
    }

    @PostMapping("/{roomId}/join")
    public MemberResponse join(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        return membershipService.join(roomId, userId);
    }

    @PostMapping("/{roomId}/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        membershipService.leave(roomId, userId);
    }
}
