package org.sirantar.recadero.promotions.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * Response payload representing a promotion rule.
 */
public record PromotionRuleResponse(
    @JsonProperty("rule_id") Long ruleId,
    @JsonProperty("promotion_id") Long promotionId,
    @JsonProperty("rule_type") String ruleType,
    @JsonProperty("condition_json") Map<String, Object> conditionJson) {}
