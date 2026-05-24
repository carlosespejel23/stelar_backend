package com.education.stelar.academic.dto.request;

import com.education.stelar.academic.entity.AttendanceStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateAttendanceRequest(

        @NotNull(message = "El estado de asistencia es obligatorio")
        AttendanceStatus status,

        @Size(max = 500, message = "La justificación no puede superar 500 caracteres")
        String justificationReason
) {
}
