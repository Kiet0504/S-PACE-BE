package com.example.S_PACE.mapper;

import com.example.S_PACE.dto.request.EventTasksRequest;
import com.example.S_PACE.dto.response.EventTasksResponse;
import com.example.S_PACE.pojo.EventTasks;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventTasksMapper {

    @Mapping(target = "teamId", source = "team.teamId")
    @Mapping(target = "teamName", source = "team.teamName")
    @Mapping(target = "assignedToUserId", source = "assignedTo.userId")
    @Mapping(target = "assignedToName", source = "assignedTo.fullName")
    @Mapping(target = "assignedToEmail", source = "assignedTo.email")
    EventTasksResponse toEventTasksResponse(EventTasks eventTasks);

    @Mapping(target = "eventTasksId", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "assignedTo", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    EventTasks toEntity(EventTasksRequest request);
}