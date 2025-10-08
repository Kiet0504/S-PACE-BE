package com.example.S_PACE.mapper;

import com.example.S_PACE.dto.request.TeamRequest;
import com.example.S_PACE.dto.response.TeamResponse;
import com.example.S_PACE.pojo.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface TeamMapper {

    @Mapping(target = "eventId", source = "event.eventId")
    TeamResponse toTeamResponse(Team team);

    @Mapping(target = "teamId", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Team toEntity(TeamRequest request);
}