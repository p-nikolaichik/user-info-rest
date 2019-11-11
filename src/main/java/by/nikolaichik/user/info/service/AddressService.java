package by.nikolaichik.user.info.service;

import by.nikolaichik.user.info.shared.dto.AddressDTO;

import java.util.List;

public interface AddressService {

    List<AddressDTO> getAddresses(String id);

    AddressDTO getUserAddress(String userId, String addressId);
}
