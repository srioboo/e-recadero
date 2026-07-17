package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A display-only snapshot of a user's address, resolved from the Users module.
 */
public record AddressSnapshot(
    @JsonProperty("street_address") String streetAddress,
    String city,
    @JsonProperty("state_province") String stateProvince,
    @JsonProperty("postal_code") String postalCode,
    @JsonProperty("country_code") String countryCode) {}
