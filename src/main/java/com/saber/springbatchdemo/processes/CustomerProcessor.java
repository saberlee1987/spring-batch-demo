package com.saber.springbatchdemo.processes;

import com.saber.springbatchdemo.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class CustomerProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer customer) throws Exception {
        log.info("customer process ===> {}", customer);
        return customer;
    }
}
