package northwind.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import northwind.config.WebServiceConfig;
import northwind.ws.client.SoapClient;

@Import({WebServiceConfig.class})
@SpringBootApplication
public class CustomerWSClientRunner implements ApplicationRunner{

	@Autowired
	private SoapClient client;
	
	public static void main(String[] args) {
//		ApplicationContext ctx = new AnnotationConfigApplicationContext(WebServiceConfig.class);
//		SoapClient client = ctx.getBean("soapClient", SoapClient.class);
//		client.getCustomer("ALFKI").ifPresent((customer)->{
//			System.out.println(customer.getCustomerName());
//		});
		SpringApplication.run(CustomerWSClientRunner.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		client.getCustomer("ALFKI").ifPresent((customer)->{
			System.out.println(customer.getCustomerName());
		});
		
	}

}
