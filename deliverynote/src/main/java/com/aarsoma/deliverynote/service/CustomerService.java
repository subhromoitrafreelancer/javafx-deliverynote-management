package com.aarsoma.deliverynote.service;

import com.aarsoma.deliverynote.model.Customer;

import com.aarsoma.deliverynote.model.Customer;
import com.aarsoma.deliverynote.repository.CustomerRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CustomerService {

    private final CustomerRepository customerRepository = new CustomerRepository();

    public List<Customer> getAllCustomers() throws SQLException {
        return customerRepository.findAll();
    }

    public Optional<Customer> getCustomerById(int id) throws SQLException {
        return customerRepository.findById(id);
    }

    public Customer saveCustomer(Customer customer) throws SQLException {
        return customerRepository.save(customer);
    }

    public void deleteCustomer(int id) throws SQLException {
        // First check if the customer is used in any delivery notes
        if (isCustomerUsedInDeliveryNotes(id)) {
            throw new SQLException("Cannot delete customer as it is used in one or more delivery notes");
        }

        customerRepository.delete(id);
    }

    public boolean isCustomerUsedInDeliveryNotes(int customerId) throws SQLException {
        return customerRepository.isReferencedInDeliveryNotes(customerId);
    }
}
