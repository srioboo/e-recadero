package org.sirantar.recadero.users.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.sirantar.recadero.shared.security.Authenticated;
import org.sirantar.recadero.shared.security.SecurityUser;
import org.sirantar.recadero.users.domain.AddressType;
import org.sirantar.recadero.users.service.AddressService;
import org.sirantar.recadero.users.service.dto.AddressRequest;
import org.sirantar.recadero.users.service.dto.AddressResponse;
import org.sirantar.recadero.users.service.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authenticated user's own address book management.
 */
@RestController
@RequestMapping("/users/me/addresses")
@RequiredArgsConstructor
@Authenticated
public class AddressController {

  private final AddressService addressService;

  @GetMapping
  public List<AddressResponse> listAddresses(
      @AuthenticationPrincipal SecurityUser user, @RequestParam(required = false) String type) {
    AddressType filter = type != null ? AddressType.valueOf(type) : null;
    return addressService.listAddresses(Long.valueOf(user.getUserId()), filter);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public AddressResponse createAddress(@AuthenticationPrincipal SecurityUser user, @RequestBody AddressRequest request) {
    return addressService.createAddress(Long.valueOf(user.getUserId()), request);
  }

  @PutMapping("/{id}")
  public AddressResponse updateAddress(
      @AuthenticationPrincipal SecurityUser user, @PathVariable Long id, @RequestBody AddressRequest request) {
    return addressService.updateAddress(Long.valueOf(user.getUserId()), id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteAddress(@AuthenticationPrincipal SecurityUser user, @PathVariable Long id) {
    addressService.deleteAddress(Long.valueOf(user.getUserId()), id);
  }

  @PutMapping("/{id}/set-primary")
  public MessageResponse setPrimary(@AuthenticationPrincipal SecurityUser user, @PathVariable Long id) {
    addressService.setPrimaryAddress(Long.valueOf(user.getUserId()), id);
    return new MessageResponse("Address set as primary");
  }

  @GetMapping("/primary/{type}")
  public AddressResponse getPrimaryAddress(@AuthenticationPrincipal SecurityUser user, @PathVariable String type) {
    return addressService.getPrimaryAddress(Long.valueOf(user.getUserId()), AddressType.valueOf(type))
        .orElseThrow(() -> new ResourceNotFoundException("No primary " + type + " address on file"));
  }
}
