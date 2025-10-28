package com.agile.board.mapper;

import com.agile.board.domain.Issue;
import com.agile.board.domain.Project;
import com.agile.board.dto.IssueDtos;
import com.agile.board.dto.ProjectDtos;
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
