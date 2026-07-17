package org.sirantar.recadero.templates.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Request payload for POST /api/v1/templates/{id}/blocks/reorder.
 */
public record ReorderBlocksRequest(List<Entry> blocks) {

  public record Entry(
      @JsonProperty("block_id") String blockId, @JsonProperty("block_order") int blockOrder) {}
}
