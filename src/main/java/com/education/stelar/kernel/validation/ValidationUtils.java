package com.education.stelar.kernel.validation;

import com.education.stelar.kernel.exception.BusinessException;

/**
 * Utilidades de validación compartidas entre módulos.
 */
public final class ValidationUtils {

    private ValidationUtils() {
    }

    public static void requireNonBlank(String value, String errorCode, String message) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(errorCode, message);
        }
    }

    public static void requireTrue(boolean condition, String errorCode, String message) {
        if (!condition) {
            throw new BusinessException(errorCode, message);
        }
    }
}
