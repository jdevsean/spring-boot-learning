package com.jdevsean.spring_boot_elasticsearch.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.jdevsean.spring_boot_elasticsearch.model.Customer;

public interface CustomerRepository extends ElasticsearchRepository<Customer, String> {
	List<Customer> findByAddress(String address);

	Customer findByUserName(String userName);

	int deleteByUserName(String userName);

	Page<Customer> findByAddress(String address, Pageable pageable);

}
