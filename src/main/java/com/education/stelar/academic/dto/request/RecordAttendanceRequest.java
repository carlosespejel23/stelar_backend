package com.education.stelar.academic.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

import com.education.stelar.academic.entity.AttendanceStatus;

public record RecordAttendanceRequest(

        @NotNull(message = "El ID de la inscripción es obligatorio")
        UUID enrollmentId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,

        @NotNull(message = "El estado de asistencia es obligatorio")
        AttendanceStatus status,

        @Size(max = 500, message = "La justificación no puede superar 500 caracteres")
        String justificationReason
) {
}
