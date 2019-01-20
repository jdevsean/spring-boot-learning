package com.jdevsean.spring_boot_elasticsearch;

import java.util.Map;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.metrics.sum.InternalSum;
import org.elasticsearch.search.aggregations.metrics.sum.SumAggregationBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.junit4.SpringRunner;

import com.jdevsean.spring_boot_elasticsearch.model.Customer;
import com.jdevsean.spring_boot_elasticsearch.repository.CustomerRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CustomerRepositoryTest {
	@Autowired
	private CustomerRepository repository;
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Test
	public void saveCustomers() {
		repository.save(new Customer("sean", "广东省广州市荔湾区墩头超市", 25));
		repository.save(new Customer("莫瑞", "广东省广州市荔湾区墩头超市", 24));
		repository.save(new Customer("莫吉托", "广东省广州市荔湾区墩头超市", 24));
	}

	@Test
	public void fetchAllCustomers() {
		System.out.println("Customers found with findAll():");
		System.out.println("-------------------------------");
		Iterable<Customer> iterable = repository.findAll();
		for (Customer customer : iterable) {
			System.out.println(customer);
		}
	}

	@Test
	public void deleteCustomers() {
		repository.deleteAll();
	}

	@Test
	public void updateCustomers() {
		Customer customer = repository.findByUserName("sean");
		System.out.println(customer);
		customer.setAge(26);
		repository.save(customer);
		Customer xcustomer = repository.findByUserName("summer");
		System.out.println(xcustomer);
	}

	/**
	 * 模糊查询
	 */
	@Test
	public void fetchIndividualCustomers() {
		System.out.println("Customer found with findByUserName('summer'):");
		System.out.println("--------------------------------");
		System.out.println(repository.findByUserName("sean"));
		System.out.println("--------------------------------");
		System.out.println("Customers found with findByAddress(\"广东省广州市荔湾区墩头超市\"):");
		String q = "广东";
		for (Customer customer : repository.findByAddress(q)) {
			System.out.println(customer);
		}
	}

	/**
	 * 使用springboot自带的排序
	 */
	@Test
	public void fetchPageCustomers() {
		Sort sort = new Sort(Sort.Direction.DESC, "address.keyword");
		Pageable pageable = PageRequest.of(0, 1, sort);
		Page<Customer> page = repository.findByAddress("广东", pageable);
		System.out.println("Page customers " + page.getContent().toString());
	}

	@Test
	public void fetchPage2Customers() {
		QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("address", "广东"));
		Page<Customer> page = repository.search(query, PageRequest.of(0, 10));
		System.out.println(page.getContent().toString());
	}

	@Test
	public void fetchAggregation() {
		QueryBuilder query = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("address", "广东"));
		SumAggregationBuilder aggr = AggregationBuilders.sum("sumAge").field("age");

		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(query).addAggregation(aggr).build();

		Aggregations aggregations = elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {

			@Override
			public Aggregations extract(SearchResponse response) {
				return response.getAggregations();
			}
		});

		// 转换成map集合
		Map<String, Aggregation> aggregationMap = aggregations.asMap();
		// 获得对应的聚合函数的聚合子类，该聚合子类也是个map集合,里面的value就是桶Bucket，我们要获得Bucket
		InternalSum sumAge = (InternalSum) aggregationMap.get("sumAge");
		System.out.println("sum age is " + sumAge.getValue());
	}

}
