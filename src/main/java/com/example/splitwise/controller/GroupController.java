package com.example.splitwise.controller;

import com.example.splitwise.dto.ApiResponse;
import com.example.splitwise.dto.CreateGroupRequest;
import com.example.splitwise.dto.GroupResponse;
import com.example.splitwise.entities.Group;
import com.example.splitwise.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        try {
            Group group = groupService.createGroup(
                    request.getGroupId(),
                    request.getGroupName(),
                    request.getCreatedByUserId()
            );
            GroupResponse response = mapToGroupResponse(group);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Group created successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroup(@PathVariable String groupId) {
        try {
            Group group = groupService.getGroupById(groupId);
            GroupResponse response = mapToGroupResponse(group);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> getAllGroups() {
        List<GroupResponse> groups = groupService.getAllGroups().stream()
                .map(this::mapToGroupResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(groups));
    }

    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<GroupResponse>> addMember(
            @PathVariable String groupId,
            @PathVariable String userId) {
        try {
            Group group = groupService.addMemberToGroup(groupId, userId);
            GroupResponse response = mapToGroupResponse(group);
            return ResponseEntity.ok(ApiResponse.success("Member added successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<GroupResponse>> removeMember(
            @PathVariable String groupId,
            @PathVariable String userId) {
        try {
            Group group = groupService.removeMemberFromGroup(groupId, userId);
            GroupResponse response = mapToGroupResponse(group);
            return ResponseEntity.ok(ApiResponse.success("Member removed successfully", response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    private GroupResponse mapToGroupResponse(Group group) {
        List<String> memberIds = group.getMembers() != null
                ? group.getMembers().stream()
                        .map(com.example.splitwise.entities.User::getUserId)
                        .collect(Collectors.toList())
                : List.of();

        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .createdByUserId(group.getCreatedBy().getUserId())
                .memberUserIds(memberIds)
                .build();
    }
}
