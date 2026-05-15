package org.sirantar.recadero.shared.api;

import org.sirantar.recadero.shared.dto.ErrorResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Base controller with shared API helpers.
 *
 * Keeps controller classes focused on domain behavior by centralizing
 * pagination defaults, sort normalization, and consistent error payloads.
 */
public abstract class ApiController {

  private static final int DEFAULT_PAGE = 0;
  private static final int DEFAULT_SIZE = 20;
  private static final int MAX_SIZE = 100;

  protected Pageable pageable(Integer page, Integer size) {
    return pageable(page, size, Sort.unsorted());
  }

  protected Pageable pageable(Integer page, Integer size, Sort sort) {
    int resolvedPage = Math.max(page != null ? page : DEFAULT_PAGE, 0);
    int resolvedSize = sanitizeSize(size);
    Sort resolvedSort = sort != null ? sort : Sort.unsorted();

    return PageRequest.of(resolvedPage, resolvedSize, resolvedSort);
  }

  protected Sort sortBy(String property, Sort.Direction direction) {
    if (property == null || property.isBlank()) {
      return Sort.unsorted();
    }

    Sort.Direction resolvedDirection = direction != null ? direction : Sort.Direction.ASC;
    return Sort.by(resolvedDirection, property);
  }

  protected <T> ResponseEntity<T> ok(T body) {
    return ResponseEntity.ok(body);
  }

  protected <T> ResponseEntity<T> created(T body) {
    return ResponseEntity.status(HttpStatus.CREATED).body(body);
  }

  protected ResponseEntity<Void> noContent() {
    return ResponseEntity.noContent().build();
  }

  protected ResponseEntity<ErrorResponse> error(
      HttpStatus status,
      String errorCode,
      String message,
      HttpServletRequest request
  ) {
    return error(status, errorCode, message, request, List.of());
  }

  protected ResponseEntity<ErrorResponse> error(
      HttpStatus status,
      String errorCode,
      String message,
      HttpServletRequest request,
      List<ErrorResponse.FieldError> fieldErrors
  ) {
    ErrorResponse response = new ErrorResponse(
        status.value(),
        errorCode,
        message,
        request.getRequestURI(),
        fieldErrors
    );

    return ResponseEntity.status(status).body(response);
  }

  private int sanitizeSize(Integer size) {
    int resolvedSize = size != null ? size : DEFAULT_SIZE;
    if (resolvedSize < 1) {
      return DEFAULT_SIZE;
    }
    return Math.min(resolvedSize, MAX_SIZE);
  }
}
