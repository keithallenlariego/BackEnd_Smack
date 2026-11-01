package com.example.securewebchatroom.controller;

import com.example.securewebchatroom.dto.RoomDto.AddMemberRequest;
import com.example.securewebchatroom.dto.RoomDto.ChangeRoleRequest;
import com.example.securewebchatroom.dto.RoomDto.MemberResponse;
import com.example.securewebchatroom.service.MembershipService;
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
@RequestMapping("/api/rooms/{roomId}/members")
public class MembershipController {

    private final MembershipService membershipService;

    public MembershipController(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    @GetMapping
    public List<MemberResponse> list(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId) {
        return membershipService.list(roomId, userId);
    }

    // Owner lets someone in (this is how people enter a private room)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse add(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId,
                              @Valid @RequestBody AddMemberRequest request) {
        return membershipService.add(roomId, userId, request.username());
    }

    @PutMapping("/{memberId}/role")
    public MemberResponse changeRole(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId,
                                     @PathVariable("memberId") Long memberId, @Valid @RequestBody ChangeRoleRequest request) {
        return membershipService.changeRole(roomId, userId, memberId, request.role());
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal Long userId, @PathVariable("roomId") Long roomId,
                       @PathVariable("memberId") Long memberId) {
        membershipService.remove(roomId, userId, memberId);
    }
}
