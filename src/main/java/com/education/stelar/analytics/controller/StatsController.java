package com.education.stelar.analytics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.education.stelar.analytics.dto.response.DashboardStatsResponse;
import com.education.stelar.analytics.dto.response.GroupStatsResponse;
import com.education.stelar.analytics.service.AcademicStatsService;
import com.education.stelar.kernel.security.CurrentUser;
import com.education.stelar.kernel.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Stats", description = "Estadísticas académicas y dashboard")
public class StatsController {

    private final AcademicStatsService academicStatsService;

    @GetMapping("/group/{groupId}")
    @Operation(summary = "Estadísticas de todas las materias de un grupo")
    public ResponseEntity<List<GroupStatsResponse>> getGroupStats(@PathVariable UUID groupId) {
        return ResponseEntity.ok(academicStatsService.getGroupStatsList(groupId));
    }

    @GetMapping("/group/{groupId}/subject/{subjectId}")
    @Operation(summary = "Estadísticas de un grupo para una materia específica")
    public ResponseEntity<GroupStatsResponse> getGroupSubjectStats(
            @PathVariable UUID groupId,
            @PathVariable UUID subjectId) {
        return ResponseEntity.ok(academicStatsService.getGroupStats(groupId, subjectId));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard general del docente autenticado")
    public ResponseEntity<DashboardStatsResponse> getDashboard(@CurrentUser UserPrincipal principal) {
        return ResponseEntity.ok(academicStatsService.getDashboard(principal.getId()));
    }
}
