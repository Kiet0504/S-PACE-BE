package com.example.S_PACE.repository;

import com.example.S_PACE.pojo.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByTeamName(String teamName);

    @Query("SELECT t FROM Team t WHERE t.event.eventId = :eventId")
    List<Team> findByEventId(@Param("eventId") UUID eventId);

    @Query("SELECT t FROM Team t WHERE t.event.eventId = :eventId AND t.teamName = :teamName")
    Optional<Team> findByEventIdAndTeamName(@Param("eventId") UUID eventId, @Param("teamName") String teamName);

    @Query("SELECT t FROM Team t LEFT JOIN FETCH t.members WHERE t.teamId = :id")
    Optional<Team> findByIdWithMembers(@Param("id") UUID id);

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members")
    List<Team> findAllWithMembers();

    @Query("SELECT DISTINCT t FROM Team t LEFT JOIN FETCH t.members WHERE t.event.eventId = :eventId")
    List<Team> findByEventIdWithMembers(@Param("eventId") UUID eventId);
}