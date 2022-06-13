package northwind.ws.client;

import java.util.Optional;

import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import northwind.domain.customer.wsdl.Customer;
import northwind.domain.customer.wsdl.GetCustomerRequest;
import northwind.domain.customer.wsdl.GetCustomerResponse;

public class SoapClient extends WebServiceGatewaySupport{

	public Optional<Customer> getCustomer(String customerId) {
		GetCustomerRequest customerRequest = new GetCustomerRequest();
		customerRequest.setCustomerID(customerId);
		
		GetCustomerResponse customerResponse = (GetCustomerResponse)getWebServiceTemplate()
		        .marshalSendAndReceive("https://localhost:8443/service", customerRequest);
		
		return Optional.of(customerResponse.getCustomer());
	}
}
