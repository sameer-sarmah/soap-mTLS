package northwind.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import northwind.config.WebServiceConfig;
//http://localhost:8888/service/customers.wsdl
@Import({WebServiceConfig.class})
@SpringBootApplication
public class CustomerServerRunner {

	public static void main(String[] args) {
		SpringApplication.run(CustomerServerRunner.class, args);
	}

}
