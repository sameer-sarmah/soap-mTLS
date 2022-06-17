package northwind.ws.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import northwind.domain.customer.Customer;
import northwind.domain.customer.CustomersPort;
import northwind.domain.customer.GetCustomerRequest;
import northwind.domain.customer.GetCustomerResponse;


@Component
public class SoapClient extends WebServiceGatewaySupport{

	@Autowired
	private CustomersPort portService; 
	
	public Optional<Customer> getCustomer(String customerId) {
		GetCustomerRequest customerRequest = new GetCustomerRequest();
		customerRequest.setCustomerID(customerId);
		
		GetCustomerResponse customerResponse = portService.getCustomer(customerRequest);
		
		return Optional.of(customerResponse.getCustomer());
	}
}
