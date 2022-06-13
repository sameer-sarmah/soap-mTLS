package northwind.repo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import northwind.domain.customer.Customer;


@Component
public class CustomerRespository {
	
	private Gson gson = new GsonBuilder().setPrettyPrinting().create();
	private List<Customer> customers = null;
	
	@PostConstruct
	public void initData() {
		customers= deserializeIntoCustomers();
	}
	
	public Customer findCustomer(String id) {
		Optional<Customer> customerOptional = customers.stream()
		.filter(customer -> customer.getCustomerID().equals(id))
		.findFirst();
		if(customerOptional.isPresent()) {
			return customerOptional.get();
		}
		else {
			return null;
		}
	}
	
	public Customer createCustomer(Customer customer) {
		customers.add(customer);
		return customer;
	}
	
	public Customer updateCustomer(Customer customerToUpdate) {
		Optional<Customer> customerOptional = customers.stream()
				.filter(customer -> customer.getCustomerID().equals(customerToUpdate.getCustomerID()))
				.findFirst();
				if(customerOptional.isPresent()) {
					Customer previousCustomerEntity =  customerOptional.get();
					previousCustomerEntity.setCity(customerToUpdate.getCity());
					previousCustomerEntity.setCountry(customerToUpdate.getCountry());
					previousCustomerEntity.setCustomerName(customerToUpdate.getCustomerName());
					previousCustomerEntity.setPhone(customerToUpdate.getPhone());
					previousCustomerEntity.setRegion(customerToUpdate.getRegion());
					previousCustomerEntity.setZip(customerToUpdate.getZip());
				}
				return null;
	}
	
	public String deleteCustomer(String id) {
		Optional<Customer> customerOptional = customers.stream()
		.filter(customer -> customer.getCustomerID().equals(id))
		.findFirst();
		if(customerOptional.isPresent()) {
			Customer customer =  customerOptional.get();
			customers.remove(customer);
			return customer.getCustomerID();
		}
		else {
			return null;
		}
	}
	
	private String getJsonString() {
		String json = null;
		try {
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("customer.json");
			json = IOUtils.toString(in, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}
	
	private List<Customer> deserializeIntoCustomers()  {
		String json = getJsonString();
		JsonParser parser = null;
		try {
			 parser = new JsonParser();
		}catch(Exception e) {
			e.printStackTrace();
		}
		JsonElement rootElement = parser.parse(json);
		if (rootElement instanceof JsonArray) {
			JsonArray customersJson = rootElement.getAsJsonArray();
			List<Customer> customers = StreamSupport.stream(
					Spliterators.spliteratorUnknownSize(customersJson.iterator(), Spliterator.ORDERED), false)
					.filter(JsonElement::isJsonObject)
					.map(JsonElement::getAsJsonObject)
					.filter(element -> element.has("CustomerID"))
					.map(CustomerRespository::convertJsonObjectToCustomer)
					.collect(Collectors.toList());
			System.out.println(customers.size());
			return customers;
		}
		return Collections.<Customer>emptyList();
	}

	public static Customer convertJsonObjectToCustomer(JsonObject json) {
		Customer customer = new Customer();
		customer.setCity(json.get("City").getAsString());
		customer.setCountry(json.get("Country").getAsString());
		customer.setCustomerID(json.get("CustomerID").getAsString());
		customer.setCustomerName(json.get("ContactName").getAsString());
		customer.setPhone(json.get("Phone").getAsString());
		JsonElement regionNode = json.get("Region");
		if (!(regionNode instanceof JsonNull)) {
			customer.setRegion(json.get("Region").getAsString());
		}
		customer.setZip(json.get("PostalCode").getAsString());
		return customer;
	}
}
