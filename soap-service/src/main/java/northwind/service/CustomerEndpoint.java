package northwind.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import northwind.domain.customer.CreateCustomerRequest;
import northwind.domain.customer.CreateCustomerResponse;
import northwind.domain.customer.DeleteCustomerRequest;
import northwind.domain.customer.DeleteCustomerResponse;
import northwind.domain.customer.GetCustomerRequest;
import northwind.domain.customer.GetCustomerResponse;
import northwind.domain.customer.UpdateCustomerRequest;
import northwind.domain.customer.UpdateCustomerResponse;
import northwind.repo.CustomerRespository;


@Endpoint
public class CustomerEndpoint {
	private static final String NAMESPACE_URI = "http://domain.northwind/customer";

	private CustomerRespository customerRepository;

	@Autowired
	public CustomerEndpoint(CustomerRespository StudentRepository) {
		this.customerRepository = StudentRepository;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "GetCustomerRequest")
	@ResponsePayload
	public GetCustomerResponse getCustomer(@RequestPayload GetCustomerRequest request) {
		GetCustomerResponse response = new GetCustomerResponse();
		response.setCustomer(customerRepository.findCustomer(request.getCustomerID()));
		return response;
	}
	
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "CreateCustomerRequest")
	@ResponsePayload
	public CreateCustomerResponse createCustomer(@RequestPayload CreateCustomerRequest request) {
		CreateCustomerResponse response = new CreateCustomerResponse();
		response.setCustomer(customerRepository.createCustomer(request.getCustomer()));
		return response;
	}
	
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "UpdateCustomerRequest")
	@ResponsePayload
	public UpdateCustomerResponse updateCustomer(@RequestPayload UpdateCustomerRequest request) {
		UpdateCustomerResponse response = new UpdateCustomerResponse();
		response.setCustomer(customerRepository.updateCustomer(request.getCustomer()));
		return response;
	}
	
	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "DeleteCustomerRequest")
	@ResponsePayload
	public DeleteCustomerResponse deleteCustomer(@RequestPayload DeleteCustomerRequest request) {
		DeleteCustomerResponse response = new DeleteCustomerResponse();
		response.setCustomerID(customerRepository.deleteCustomer(request.getCustomerID()));
		return response;
	}
}
