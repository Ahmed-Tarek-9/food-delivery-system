package com.masrdelivery.repository;

import com.masrdelivery.domain.common.MobileNumber;
import com.masrdelivery.domain.customer.Customer;
import com.masrdelivery.exception.CustomerNotFoundException;
import com.masrdelivery.exception.DuplicateEntityException;
import com.masrdelivery.exception.InvalidStringException;
import com.masrdelivery.exception.NullEntityException;

import java.util.*;

public class CustomerRepository implements Repository<String, Customer> {

    private final Map<String, Customer> customers;

    public CustomerRepository() {
        this.customers = new HashMap<>();
    }

    @Override
    public void save(Customer customer) {
        if (customer == null) {
            throw new NullEntityException("Customer");
        }

        if (customers.containsKey(customer.getId())) {
            throw new DuplicateEntityException("Customer");
        }

        customers.put(customer.getId(), customer);
    }

    @Override
    public void update(Customer customer) {
        if (customer == null) {
            throw new NullEntityException("Customer");
        }

        if (!customers.containsKey(customer.getId())) {
            throw new CustomerNotFoundException("Customer not found");
        }
        customers.put(customer.getId(), customer);
    }

    @Override
    public Optional<Customer> findById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Customer ID cannot be null or empty");
        }
        return Optional.ofNullable(customers.get(id));
    }

    @Override
    public Collection<Customer> findAll() {
        return List.copyOf(customers.values());
    }

    @Override
    public void deleteById(String id) {
        if (id == null || id.isBlank()) {
            throw new InvalidStringException("Customer ID cannot be null or empty");
        }
        customers.remove(id);
    }

    public boolean existsByMobileNumber(MobileNumber mobileNumber) {
        return customers.values()
                .stream()
                .anyMatch(
                        customer ->
                                customer.getMobileNumber().equals(mobileNumber)
                );
    }
}
