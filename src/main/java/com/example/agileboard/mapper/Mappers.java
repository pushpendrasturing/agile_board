package com.example.agileboard.mapper;

import com.example.agileboard.domain.*;
import com.example.agileboard.dto.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface Mappers {
    @Mapping(
            target = "memberIds",
            expression = "java(project.getMembers() == null ? java.util.Collections.emptySet() : project.getMembers().stream().map(User::getId).collect(java.util.stream.Collectors.toSet()))"
    )
    ProjectDtos.ProjectView toView(Project project);


    @Mapping(target = "projectId", source = "project.id")
    @Mapping(target = "assigneeId", source = "assignee.id")
    IssueDtos.IssueView toView(Issue issue);
}
