package org.sirantar.recadero.shared.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Generic pagination response wrapper for list endpoints.
 *
 * Wraps paginated data with metadata:
 * - data: list of items on current page
 * - page: current page index (zero-based)
 * - size: items per page
 * - total_elements: total count of items across all pages
 * - total_pages: total number of pages
 * - is_first: whether this is the first page
 * - is_last: whether this is the last page
 * - has_next: whether there are more pages
 * - has_previous: whether there are previous pages
 *
 * @param <T> type of items in the paginated list
 */
public class PaginationResponse<T> {

  @JsonProperty("data")
  private List<T> data;

  @JsonProperty("page")
  private int page;

  @JsonProperty("size")
  private int size;

  @JsonProperty("total_elements")
  private long totalElements;

  @JsonProperty("total_pages")
  private int totalPages;

  @JsonProperty("is_first")
  private boolean isFirst;

  @JsonProperty("is_last")
  private boolean isLast;

  @JsonProperty("has_next")
  private boolean hasNext;

  @JsonProperty("has_previous")
  private boolean hasPrevious;

  public PaginationResponse() {
  }

  public PaginationResponse(
      List<T> data,
      int page,
      int size,
      long totalElements
  ) {
    this.data = data;
    this.page = page;
    this.size = size;
    this.totalElements = totalElements;
    this.totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
    this.isFirst = page == 0;
    this.isLast = page >= (totalPages - 1);
    this.hasNext = page < (totalPages - 1);
    this.hasPrevious = page > 0;
  }

  /**
   * Factory method to create a response from Spring Data Page object.
   *
   * @param springPage Spring Data Page object
   * @param <T> type of items
   * @return PaginationResponse wrapping the page data
   */
  public static <T> PaginationResponse<T> from(org.springframework.data.domain.Page<T> springPage) {
    return new PaginationResponse<>(
        springPage.getContent(),
        springPage.getNumber(),
        springPage.getSize(),
        springPage.getTotalElements()
    );
  }

  public List<T> getData() {
    return data;
  }

  public void setData(List<T> data) {
    this.data = data;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public long getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(long totalElements) {
    this.totalElements = totalElements;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }

  public boolean isFirst() {
    return isFirst;
  }

  public void setFirst(boolean first) {
    isFirst = first;
  }

  public boolean isLast() {
    return isLast;
  }

  public void setLast(boolean last) {
    isLast = last;
  }

  public boolean isHasNext() {
    return hasNext;
  }

  public void setHasNext(boolean hasNext) {
    this.hasNext = hasNext;
  }

  public boolean isHasPrevious() {
    return hasPrevious;
  }

  public void setHasPrevious(boolean hasPrevious) {
    this.hasPrevious = hasPrevious;
  }

  @Override
  public String toString() {
    return "PaginationResponse{" +
        "page=" + page +
        ", size=" + size +
        ", total_elements=" + totalElements +
        ", total_pages=" + totalPages +
        ", is_first=" + isFirst +
        ", is_last=" + isLast +
        ", has_next=" + hasNext +
        ", has_previous=" + hasPrevious +
        ", data_count=" + (data != null ? data.size() : 0) +
        '}';
  }
}
