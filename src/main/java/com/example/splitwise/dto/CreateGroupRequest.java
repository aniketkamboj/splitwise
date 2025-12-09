package com.example.splitwise.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupRequest {
    
    @NotBlank(message = "Group ID is required")
    private String groupId;
    
    @NotBlank(message = "Group name is required")
    private String groupName;
    
    @NotBlank(message = "Created by user ID is required")
    private String createdByUserId;
}

