package northwind.app;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.ws.client.WebServiceTransportException;

import northwind.config.CxfConfig;
import northwind.domain.customer.GetCustomerRequest;
import northwind.routebuilder.SoapMtlsRouteBuilder;

@Import({CxfConfig.class})
@SpringBootApplication
public class CustomerCamelMTLSSoapClientRunner implements ApplicationRunner{
	
	@Autowired
	private SoapMtlsRouteBuilder routeBuilder;
	
	@Autowired 
	private ProducerTemplate producerTemplate;
	
	@Autowired
	private CamelContext context ;
	
	public static void main(String[] args) {
		SpringApplication.run(CustomerCamelMTLSSoapClientRunner.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try {
			context.addRoutes(routeBuilder);
	        context.start();
	        
			GetCustomerRequest customerRequest = new GetCustomerRequest();
			customerRequest.setCustomerID("ALFKI");
			producerTemplate.sendBody("direct:start", customerRequest);
	        Thread.sleep(10000);
	        context.stop();

		}
		catch (Exception e) {
			if(e instanceof WebServiceTransportException) {
				System.err.println("[WebServiceTransportException]	ExceptionMessage="+e.getMessage());
			}
			else {
				System.err.println("ExceptionMessage="+e.getMessage());
			}
		}
	}

}
