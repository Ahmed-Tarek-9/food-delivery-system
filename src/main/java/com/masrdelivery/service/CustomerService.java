package com.masrdelivery.service;

import com.masrdelivery.domain.common.Address;
import com.masrdelivery.domain.common.MobileNumber;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.exception.*;
import com.masrdelivery.repository.CustomerRepository;
import com.masrdelivery.domain.common.id.CustomerIdGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerIdGenerator customerIdGenerator;

    public CustomerService(CustomerRepository customerRepository, CustomerIdGenerator customerIdGenerator) {
        if (customerIdGenerator == null)
            throw new NullEntityException("CustomerIdGenerator");
        if (customerRepository == null)
            throw new NullEntityException("CustomerRepository");
        this.customerRepository = customerRepository;
        this.customerIdGenerator = customerIdGenerator;
    }

    public Customer createCustomer(String name, String mobileNumber, Address address) {
        MobileNumber number = new MobileNumber(mobileNumber);
        if (customerRepository.existsByMobileNumber(number)) {
            throw new DuplicateMobileNumberException("This mobile number was already used for another customer.");
        }
        Customer customer = new Customer.Builder()
                .name(name)
                .mobileNumber(number)
                .id(customerIdGenerator.generate())
                .address(address)
                .build();

        customerRepository.save(customer);

        return customer;
    }

    public void addAddress(String customerId, Address address) {
        Customer customer = findCustomer(customerId);
        customer.addAddress(address);
    }

    public void removeAddress(String customerId, Address address) {
        Customer customer = findCustomer(customerId);
        customer.removeAddress(address);
    }

    public void rechargeWallet(String customerId, BigDecimal amount) {
        Customer customer = findCustomer(customerId);
        customer.rechargeWallet(amount);
    }

    public void updateMobileNumber(String customerId, String mobileNumber) {
        MobileNumber number = new MobileNumber(mobileNumber);
        if (customerRepository.existsByMobileNumber(number)) {
            throw new DuplicateMobileNumberException("This mobile number was already used for another customer.");
        }
        Customer customer = findCustomer(customerId);
        customer.updateMobileNumber(number);

    }

    public Customer findCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw new InvalidStringException("Customer ID cannot be null or blank");
        }

        return customerRepository.findById(customerId).orElseThrow(
                () -> new CustomerNotFoundException(customerId)
        );
    }

    public List<Customer> listCustomers() {
        return new ArrayList<>(customerRepository.findAll());
    }

    public void addSearch(String customerId, String search) {
        Customer customer = findCustomer(customerId);
        customer.addSearch(search);
    }

    public List<String> getRecentSearches(String customerId) {
        Customer customer = findCustomer(customerId);
        return customer.getRecentSearches();
    }
}
