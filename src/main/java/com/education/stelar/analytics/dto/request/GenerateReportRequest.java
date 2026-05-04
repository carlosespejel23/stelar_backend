package com.education.stelar.analytics.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

import com.education.stelar.analytics.entity.ReportType;

public record GenerateReportRequest(

        @NotNull(message = "El tipo de reporte es obligatorio")
        ReportType reportType,

        @Size(max = 255, message = "El título no puede superar 255 caracteres")
        String title,

        UUID groupId,
        UUID subjectId,
        UUID studentId,
        UUID academicPeriodId
) {
}
