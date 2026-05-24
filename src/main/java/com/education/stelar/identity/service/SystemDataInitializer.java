package com.education.stelar.identity.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.education.stelar.identity.entity.Permission;
import com.education.stelar.identity.repository.PermissionRepository;

import java.util.List;

/**
 * Siembra los permisos del sistema al arrancar la aplicación.
 * Idempotente: si el permiso ya existe (por código único), lo omite.
 * No crea roles — los roles del sistema se crean por tenant en AuthService.register().
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemDataInitializer implements ApplicationRunner {

    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initializePermissions();
    }

    private void initializePermissions() {
        List<PermissionDef> defs = buildPermissionDefs();
        int created = 0;
        for (PermissionDef def : defs) {
            if (permissionRepository.findByCode(def.code()).isEmpty()) {
                permissionRepository.save(
                        Permission.create(def.code(), def.description(), def.category(),
                                def.action(), def.resource(), "FULL_STACK")
                );
                created++;
            }
        }
        if (created > 0) {
            log.info("SystemDataInitializer: {} permisos del sistema creados", created);
        }
    }

    private List<PermissionDef> buildPermissionDefs() {
        return List.of(
                // Estudiantes
                new PermissionDef("STUDENTS_VIEW",          "Ver estudiantes",                  "STUDENTS",    "VIEW",        "students"),
                new PermissionDef("STUDENTS_CREATE",        "Crear estudiantes",                "STUDENTS",    "CREATE",      "students"),
                new PermissionDef("STUDENTS_EDIT",          "Editar estudiantes",               "STUDENTS",    "EDIT",        "students"),
                new PermissionDef("STUDENTS_DELETE",        "Eliminar estudiantes",             "STUDENTS",    "DELETE",      "students"),
                new PermissionDef("STUDENTS_FULL_ACCESS",   "Acceso completo a estudiantes",    "STUDENTS",    "FULL_ACCESS", "students"),
                // Grupos
                new PermissionDef("GROUPS_VIEW",            "Ver grupos",                       "GROUPS",      "VIEW",        "groups"),
                new PermissionDef("GROUPS_CREATE",          "Crear grupos",                     "GROUPS",      "CREATE",      "groups"),
                new PermissionDef("GROUPS_EDIT",            "Editar grupos",                    "GROUPS",      "EDIT",        "groups"),
                new PermissionDef("GROUPS_DELETE",          "Eliminar grupos",                  "GROUPS",      "DELETE",      "groups"),
                new PermissionDef("GROUPS_FULL_ACCESS",     "Acceso completo a grupos",         "GROUPS",      "FULL_ACCESS", "groups"),
                // Materias
                new PermissionDef("SUBJECTS_VIEW",          "Ver materias",                     "SUBJECTS",    "VIEW",        "subjects"),
                new PermissionDef("SUBJECTS_CREATE",        "Crear materias",                   "SUBJECTS",    "CREATE",      "subjects"),
                new PermissionDef("SUBJECTS_EDIT",          "Editar materias",                  "SUBJECTS",    "EDIT",        "subjects"),
                new PermissionDef("SUBJECTS_DELETE",        "Eliminar materias",                "SUBJECTS",    "DELETE",      "subjects"),
                new PermissionDef("SUBJECTS_FULL_ACCESS",   "Acceso completo a materias",       "SUBJECTS",    "FULL_ACCESS", "subjects"),
                // Calificaciones
                new PermissionDef("GRADES_VIEW",            "Ver calificaciones",               "GRADES",      "VIEW",        "grades"),
                new PermissionDef("GRADES_CREATE",          "Registrar calificaciones",         "GRADES",      "CREATE",      "grades"),
                new PermissionDef("GRADES_EDIT",            "Editar calificaciones",            "GRADES",      "EDIT",        "grades"),
                new PermissionDef("GRADES_FULL_ACCESS",     "Acceso completo a calificaciones", "GRADES",      "FULL_ACCESS", "grades"),
                // Asistencia
                new PermissionDef("ATTENDANCE_VIEW",        "Ver asistencia",                   "ATTENDANCE",  "VIEW",        "attendance"),
                new PermissionDef("ATTENDANCE_CREATE",      "Registrar asistencia",             "ATTENDANCE",  "CREATE",      "attendance"),
                new PermissionDef("ATTENDANCE_EDIT",        "Editar asistencia",                "ATTENDANCE",  "EDIT",        "attendance"),
                new PermissionDef("ATTENDANCE_FULL_ACCESS", "Acceso completo a asistencia",     "ATTENDANCE",  "FULL_ACCESS", "attendance"),
                // Alertas
                new PermissionDef("ALERTS_VIEW",            "Ver alertas",                      "ALERTS",      "VIEW",        "alerts"),
                new PermissionDef("ALERTS_DISMISS",         "Descartar alertas",                "ALERTS",      "DISMISS",     "alerts"),
                new PermissionDef("ALERTS_EVALUATE",        "Evaluar alertas",                  "ALERTS",      "EVALUATE",    "alerts"),
                new PermissionDef("ALERTS_FULL_ACCESS",     "Acceso completo a alertas",        "ALERTS",      "FULL_ACCESS", "alerts"),
                // Reportes
                new PermissionDef("REPORTS_VIEW",           "Ver reportes",                     "REPORTS",     "VIEW",        "reports"),
                new PermissionDef("REPORTS_GENERATE",       "Generar reportes",                 "REPORTS",     "GENERATE",    "reports"),
                new PermissionDef("REPORTS_FULL_ACCESS",    "Acceso completo a reportes",       "REPORTS",     "FULL_ACCESS", "reports"),
                // Usuarios
                new PermissionDef("USERS_VIEW",             "Ver usuarios",                     "USERS",       "VIEW",        "users"),
                new PermissionDef("USERS_CREATE",           "Crear usuarios",                   "USERS",       "CREATE",      "users"),
                new PermissionDef("USERS_EDIT",             "Editar usuarios",                  "USERS",       "EDIT",        "users"),
                new PermissionDef("USERS_DELETE",           "Eliminar usuarios",                "USERS",       "DELETE",      "users"),
                new PermissionDef("USERS_INVITE",           "Invitar usuarios",                 "USERS",       "INVITE",      "users"),
                new PermissionDef("USERS_FULL_ACCESS",      "Acceso completo a usuarios",       "USERS",       "FULL_ACCESS", "users")
        );
    }

    private record PermissionDef(
            String code, String description, String category, String action, String resource) {
    }
}
