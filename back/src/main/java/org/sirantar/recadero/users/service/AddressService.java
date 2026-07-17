package org.sirantar.recadero.users.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.users.domain.Address;
import org.sirantar.recadero.users.domain.AddressType;
import org.sirantar.recadero.users.repository.AddressRepository;
import org.sirantar.recadero.users.service.dto.AddressRequest;
import org.sirantar.recadero.users.service.dto.AddressResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages a user's billing/shipping addresses.
 */
@Service
@RequiredArgsConstructor
public class AddressService {

  private final AddressRepository addressRepository;

  public List<AddressResponse> listAddresses(Long userId, AddressType type) {
    List<Address> addresses =
        type != null ? addressRepository.findByUserIdAndType(userId, type) : addressRepository.findByUserId(userId);
    return addresses.stream().map(this::toResponse).toList();
  }

  @Transactional
  public AddressResponse createAddress(Long userId, AddressRequest request) {
    Address address = new Address();
    address.setUserId(userId);
    applyRequest(address, request);
    LocalDateTime now = LocalDateTime.now();
    address.setCreatedAt(now);
    address.setUpdatedAt(now);

    if (Boolean.TRUE.equals(address.getIsPrimary())) {
      clearExistingPrimary(userId, address.getType());
    }

    return toResponse(addressRepository.save(address));
  }

  @Transactional
  public AddressResponse updateAddress(Long userId, Long addressId, AddressRequest request) {
    Address address = getOwnedAddress(userId, addressId);
    applyRequest(address, request);
    address.setUpdatedAt(LocalDateTime.now());

    if (Boolean.TRUE.equals(address.getIsPrimary())) {
      clearExistingPrimary(userId, address.getType(), addressId);
    }

    return toResponse(addressRepository.save(address));
  }

  @Transactional
  public void deleteAddress(Long userId, Long addressId) {
    Address address = getOwnedAddress(userId, addressId);
    addressRepository.delete(address);
  }

  @Transactional
  public void setPrimaryAddress(Long userId, Long addressId) {
    Address address = getOwnedAddress(userId, addressId);
    clearExistingPrimary(userId, address.getType(), addressId);
    address.setIsPrimary(true);
    address.setUpdatedAt(LocalDateTime.now());
    addressRepository.save(address);
  }

  public Optional<AddressResponse> getPrimaryAddress(Long userId, AddressType type) {
    return addressRepository.findByUserIdAndIsPrimaryAndType(userId, true, type).map(this::toResponse);
  }

  private Address getOwnedAddress(Long userId, Long addressId) {
    Address address = addressRepository.findById(addressId)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
    if (!address.getUserId().equals(userId)) {
      throw new ResourceNotFoundException("Address not found: " + addressId);
    }
    return address;
  }

  private void clearExistingPrimary(Long userId, AddressType type) {
    clearExistingPrimary(userId, type, null);
  }

  private void clearExistingPrimary(Long userId, AddressType type, Long excludeAddressId) {
    addressRepository.findByUserIdAndIsPrimaryAndType(userId, true, type)
        .filter(existing -> excludeAddressId == null || !existing.getId().equals(excludeAddressId))
        .ifPresent(existing -> {
          existing.setIsPrimary(false);
          addressRepository.save(existing);
        });
  }

  private void applyRequest(Address address, AddressRequest request) {
    address.setType(AddressType.valueOf(request.type()));
    address.setStreetAddress(request.streetAddress());
    address.setStreetAddress2(request.streetAddress2());
    address.setCity(request.city());
    address.setStateProvince(request.stateProvince());
    address.setPostalCode(request.postalCode());
    address.setCountryCode(request.countryCode());
    address.setIsPrimary(Boolean.TRUE.equals(request.isPrimary()));
  }

  private AddressResponse toResponse(Address address) {
    return new AddressResponse(
        address.getId(),
        address.getType().name(),
        address.getStreetAddress(),
        address.getStreetAddress2(),
        address.getCity(),
        address.getStateProvince(),
        address.getPostalCode(),
        address.getCountryCode(),
        Boolean.TRUE.equals(address.getIsPrimary()),
        address.getCreatedAt());
  }
}
