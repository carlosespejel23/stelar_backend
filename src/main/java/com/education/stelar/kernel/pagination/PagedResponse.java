package com.education.stelar.kernel.pagination;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Wrapper genérico para respuestas paginadas.
 * Uso: PagedResponse.from(page, UserResponse::from)
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T, E> PagedResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}
