package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Request payload for a single promotion rule (embedded in create, or POST .../rules).
 */
public record PromotionRuleRequest(
    @JsonProperty("rule_type") String ruleType, @JsonProperty("condition_json") Map<String, Object> conditionJson) {}
