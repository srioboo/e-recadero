package org.sirantar.recadero.users.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for creating/updating an address.
 */
public record AddressRequest(
    String type,
    @JsonProperty("street_address") String streetAddress,
    @JsonProperty("street_address_2") String streetAddress2,
    String city,
    @JsonProperty("state_province") String stateProvince,
    @JsonProperty("postal_code") String postalCode,
    @JsonProperty("country_code") String countryCode,
    @JsonProperty("is_primary") Boolean isPrimary) {}
