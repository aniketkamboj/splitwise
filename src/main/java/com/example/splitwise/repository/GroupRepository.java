package com.example.splitwise.repository;

import com.example.splitwise.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByGroupId(String groupId);
    boolean existsByGroupId(String groupId);
}

