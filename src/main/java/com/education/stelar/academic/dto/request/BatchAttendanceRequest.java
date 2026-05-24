package com.education.stelar.academic.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.education.stelar.academic.entity.AttendanceStatus;

public record BatchAttendanceRequest(

        @NotNull(message = "El ID del grupo es obligatorio")
        UUID groupId,

        @NotNull(message = "El ID de la materia es obligatorio")
        UUID subjectId,

        @NotNull(message = "La fecha es obligatoria")
        LocalDate date,

        @NotEmpty(message = "Debe incluir al menos un registro de asistencia")
        @Valid
        List<StudentAttendanceEntry> records
) {
    public record StudentAttendanceEntry(

            @NotNull(message = "El ID del estudiante es obligatorio")
            UUID studentId,

            @NotNull(message = "El estado de asistencia es obligatorio")
            AttendanceStatus status,

            @Size(max = 500, message = "La justificación no puede superar 500 caracteres")
            String justificationReason
    ) {
    }
}
