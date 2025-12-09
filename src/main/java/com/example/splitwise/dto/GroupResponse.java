package com.example.splitwise.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupResponse {
    private String groupId;
    private String groupName;
    private String createdByUserId;
    private List<String> memberUserIds;
}

