package org.sirantar.recadero.users.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.users.domain.Address;
import org.sirantar.recadero.users.domain.AddressType;
import org.sirantar.recadero.users.repository.AddressRepository;
import org.sirantar.recadero.users.service.dto.AddressRequest;
import org.sirantar.recadero.users.service.dto.AddressResponse;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

  @Mock private AddressRepository addressRepository;

  @InjectMocks private AddressService addressService;

  private Address existingPrimary;

  @BeforeEach
  void setUp() {
    existingPrimary = new Address();
    existingPrimary.setId(1L);
    existingPrimary.setUserId(10L);
    existingPrimary.setType(AddressType.SHIPPING);
    existingPrimary.setIsPrimary(true);
  }

  @Test
  void createAddressUnsetsPreviousPrimaryOfSameType() {
    AddressRequest request = new AddressRequest(
        "SHIPPING", "123 Main St", null, "Springfield", "IL", "62701", "US", true);

    when(addressRepository.findByUserIdAndIsPrimaryAndType(10L, true, AddressType.SHIPPING))
        .thenReturn(Optional.of(existingPrimary));
    when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

    addressService.createAddress(10L, request);

    ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
    verify(addressRepository, org.mockito.Mockito.times(2)).save(captor.capture());

    Address demoted = captor.getAllValues().get(0);
    assertThat(demoted.getId()).isEqualTo(1L);
    assertThat(demoted.getIsPrimary()).isFalse();
  }

  @Test
  void listAddressesFiltersByType() {
    when(addressRepository.findByUserIdAndType(10L, AddressType.BILLING)).thenReturn(List.of(existingPrimary));

    List<AddressResponse> result = addressService.listAddresses(10L, AddressType.BILLING);

    assertThat(result).hasSize(1);
  }

  @Test
  void listAddressesReturnsAllWhenTypeIsNull() {
    when(addressRepository.findByUserId(10L)).thenReturn(List.of(existingPrimary));

    List<AddressResponse> result = addressService.listAddresses(10L, null);

    assertThat(result).hasSize(1);
  }
}
