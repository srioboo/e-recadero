package org.sirantar.recadero.promotions.domain;

/**
 * How a promotion's rules combine: every rule must pass (ALL/AND), or any one (ANY/OR).
 */
public enum RuleMatchMode {
  ALL,
  ANY
}
