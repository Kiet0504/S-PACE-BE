package com.example.S_PACE.mapper;

import com.example.S_PACE.dto.request.EventRequest;
import com.example.S_PACE.dto.response.EventResponse;
import com.example.S_PACE.pojo.Event;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {

    @Mapping(source = "title", target = "eventName")
    EventResponse toEventResponse(Event event);

    List<EventResponse> toEventResponseList(List<Event> events);

    @Mapping(source = "eventName", target = "title")
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Event toEvent(EventRequest eventRequest);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "eventName", target = "title")
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "company", ignore = true)
    @Mapping(target = "teams", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEventFromRequest(EventRequest eventRequest, @MappingTarget Event event);
}