package com.example.splitwise.service;

import com.example.splitwise.entities.Group;
import com.example.splitwise.entities.User;
import com.example.splitwise.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserService userService;

    @Transactional
    public Group createGroup(String groupId, String groupName, String createdByUserId) {
        if (groupRepository.existsByGroupId(groupId)) {
            throw new IllegalArgumentException("Group with groupId " + groupId + " already exists");
        }

        User createdBy = userService.getUserById(createdByUserId);

        Group group = Group.builder()
                .groupId(groupId)
                .groupName(groupName)
                .createdBy(createdBy)
                .members(new java.util.ArrayList<>())
                .expenses(new java.util.ArrayList<>())
                .build();

        group.addMember(createdBy);

        return groupRepository.save(group);
    }

    public Group getGroupById(String groupId) {
        return groupRepository.findByGroupId(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with groupId: " + groupId));
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    @Transactional
    public Group addMemberToGroup(String groupId, String userId) {
        Group group = getGroupById(groupId);
        User user = userService.getUserById(userId);

        group.addMember(user);
        return groupRepository.save(group);
    }

    @Transactional
    public Group removeMemberFromGroup(String groupId, String userId) {
        Group group = getGroupById(groupId);
        User user = userService.getUserById(userId);

        group.removeMember(user);
        return groupRepository.save(group);
    }
}

