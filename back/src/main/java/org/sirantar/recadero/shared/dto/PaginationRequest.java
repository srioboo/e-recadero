package org.sirantar.recadero.shared.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Standard pagination request DTO for list endpoints.
 *
 * Used to accept pagination parameters from clients:
 * - page: zero-based page index (default: 0)
 * - size: items per page, capped at 100 (default: 20)
 * - sort: optional sort criteria in format "field,direction" (e.g., "name,ASC")
 */
public class PaginationRequest {

  @Min(value = 0, message = "Page must be >= 0")
  @JsonProperty("page")
  private Integer page = 0;

  @Min(value = 1, message = "Size must be >= 1")
  @Max(value = 100, message = "Size must be <= 100")
  @JsonProperty("size")
  private Integer size = 20;

  @JsonProperty("sort")
  private List<String> sort;

  public PaginationRequest() {
  }

  public PaginationRequest(Integer page, Integer size) {
    this(page, size, null);
  }

  public PaginationRequest(Integer page, Integer size, List<String> sort) {
    this.page = page != null && page >= 0 ? page : 0;
    this.size = (size != null && size >= 1 && size <= 100) ? size : 20;
    this.sort = sort;
  }

  public Integer getPage() {
    return page != null && page >= 0 ? page : 0;
  }

  public void setPage(Integer page) {
    this.page = page != null && page >= 0 ? page : 0;
  }

  public Integer getSize() {
    if (size == null || size < 1) {
      return 20;
    }
    return Math.min(size, 100);
  }

  public void setSize(Integer size) {
    this.size = (size != null && size >= 1) ? Math.min(size, 100) : 20;
  }

  public List<String> getSort() {
    return sort;
  }

  public void setSort(List<String> sort) {
    this.sort = sort;
  }

  @Override
  public String toString() {
    return "PaginationRequest{" +
        "page=" + getPage() +
        ", size=" + getSize() +
        ", sort=" + sort +
        '}';
  }
}
