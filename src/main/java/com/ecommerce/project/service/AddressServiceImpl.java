package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.utils.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private UserRepository userRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {

        Address address=modelMapper.map(addressDTO,Address.class);
        List<Address> addressList=user.getAddresses();
        addressList.add(address);
        user.setAddresses(addressList);

        address.setUser(user);
        Address savedAddress=addressRepository.save(address);

        return modelMapper.map(savedAddress,AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address> addressList=addressRepository.findAll();
        List<AddressDTO> addressDTOList=addressList.stream().map(a->{
            AddressDTO addressDTO=modelMapper.map(a,AddressDTO.class);
            return addressDTO;
        }).toList();
        return addressDTOList;
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));
        AddressDTO addressDTO=modelMapper.map(address,AddressDTO.class);
        return addressDTO;
    }

    @Override
    public List<AddressDTO> getAddressesByUser(User user) {
        List<Address> addressList=user.getAddresses();
        return addressList.stream().map(a->
            modelMapper.map(a,AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO updateAddressById(Long addressId, AddressDTO addressDTO) {
        Address userAddress=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        userAddress.setBuildingName(addressDTO.getBuildingName());
        userAddress.setCity(addressDTO.getCity());
        userAddress.setCountry(addressDTO.getCountry());
        userAddress.setPincode(addressDTO.getPincode());
        userAddress.setState(addressDTO.getState());
        userAddress.setStreet(addressDTO.getStreet());

        Address updatedAddress=addressRepository.save(userAddress);

        User user=userAddress.getUser();
        user.getAddresses().removeIf(address->address.getAddressId().equals(addressId));
        user.getAddresses().add(updatedAddress);
        userRepository.save(user);
        return modelMapper.map(updatedAddress,AddressDTO.class);
    }

    @Override
    public String deleteAddressById(Long addressId) {
        Address address=addressRepository.findById(addressId)
                .orElseThrow(()->new ResourceNotFoundException("Address","addressId",addressId));

        User user=address.getUser();
        user.getAddresses().removeIf(a->a.getAddressId().equals(addressId));
        userRepository.save(user);

        addressRepository.delete(address);

        return "Address deleted successfully with addressId : "+addressId;
    }
}
