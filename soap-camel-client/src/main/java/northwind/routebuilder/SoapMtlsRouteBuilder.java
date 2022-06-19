package northwind.routebuilder;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.jsse.KeyManagersParameters;
import org.apache.camel.support.jsse.KeyStoreParameters;
import org.apache.camel.support.jsse.SSLContextParameters;
import org.apache.camel.support.jsse.TrustManagersParameters;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import northwind.cxf.configurer.DisableCNVerificationConfigurer;
import northwind.domain.customer.GetCustomerResponse;
import northwind.util.KeyStoreUtil;
import northwind.util.SecurityUtil;

@Component
public class SoapMtlsRouteBuilder extends RouteBuilder {

	@Value("${key-store-file}")
	private String keyStoreFile;
	@Value("${server.ssl.key-store-password}")
	private String keyStorePwd;
	@Value("${server.ssl.key-password}")
	private String keyPwd;
	@Value("${server.ssl.key-store-type}")
	private String keyStoreType;

	@Value("${trust-store-file}")
	private String trustStoreFile;
	@Value("${server.ssl.trust-store-password}")
	private String trustStorePwd;
	@Value("${server.ssl.trust-store-password}")
	private String trustStoreKeyPwd;
	@Value("${server.ssl.trust-store-type}")
	private String trustStoreType;
	@Autowired
	private KeyStoreUtil keyStoreUtil;

	@Override
	public void configure() throws Exception {
		String endpoint = "cxf://https://localhost:8443/service/customers?serviceClass=northwind.domain.customer.CustomersPort"
				+ "&serviceName=CustomersPortService&wsdlURL=Customer-SSL.wsdl"
				+"&sslContextParameters=#sslContext&cxfConfigurer=#disableCNVerificationConfigurer";

		System.out.println(endpoint);
		SSLContextParameters sslContext = new SSLContextParameters() ;


		configureKeyStore(sslContext);
		configureTrustStore(sslContext);
		DisableCNVerificationConfigurer disableCNVerificationConfigurer = new DisableCNVerificationConfigurer();
		Registry registry =getContext().getRegistry();
		registry.bind("sslContext", sslContext);
		registry.bind("disableCNVerificationConfigurer", disableCNVerificationConfigurer);

		
		from("direct:start").to(endpoint).process((exchange) -> {
			if (Objects.nonNull(exchange.getIn()) && Objects.nonNull(exchange.getIn().getBody())) {
				Object response = exchange.getIn().getBody();
				if (response instanceof List) {
					List contents = (List) response;
					if (contents.size() > 0) {
						Optional<GetCustomerResponse> customerResponseOptional = contents.stream()
								.filter(content -> content instanceof GetCustomerResponse).findFirst();
						customerResponseOptional.ifPresent(customerResponse -> {
							System.out.println("CustomerName=" + customerResponse.getCustomer().getCustomerName());
						});
					}

				}
			}
		});
	}

	/*
	 * KeyManagersParameters.createKeyManagers is invoked by org.apache.camel.support.jsse.SSLContextParameters.createSSLContext(CamelContext)
	 * SSLContextParameters.createSSLContext is the most important method which establishing SSL conection
	 * */
	private void configureKeyStore(SSLContextParameters sslContext) {
		KeyManagersParameters keyManagersParameters = Objects.nonNull(sslContext.getKeyManagers()) ? sslContext.getKeyManagers() : new KeyManagersParameters() ;
		KeyStoreParameters keyStoreParameters = Objects.nonNull(keyManagersParameters.getKeyStore()) ? keyManagersParameters.getKeyStore() : new KeyStoreParameters() ;
		KeyStore keyStore;
		try {
			keyStore = readKeyStore(keyStoreFile, keyStorePwd, keyStoreType);
			SecurityUtil.analyseKeystore(keyStore, List.of("client", "server"), "client");
			keyStoreParameters.setKeyStore(keyStore);
			keyStoreParameters.setPassword(keyStorePwd);
			keyStoreParameters.setType(keyStoreType);
			keyManagersParameters.setKeyStore(keyStoreParameters);
			keyManagersParameters.setKeyPassword(keyStorePwd);
			sslContext.setKeyManagers(keyManagersParameters);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * TrustManagersParameters.createKeyManagers is invoked by org.apache.camel.support.jsse.SSLContextParameters.createSSLContext(CamelContext)
	 * SSLContextParameters.createSSLContext is the most important method which establishing SSL conection
	 * */
	private void configureTrustStore(SSLContextParameters sslContext) {

		TrustManagersParameters trustManagersParameters = Objects.nonNull(sslContext.getTrustManagers()) ? sslContext.getTrustManagers() : new TrustManagersParameters() ;
		KeyStoreParameters trustStoreParameters = Objects.nonNull(trustManagersParameters.getKeyStore()) ? trustManagersParameters.getKeyStore() : new KeyStoreParameters() ;
		KeyStore trustStore;
		try {
			trustStore = readKeyStore(trustStoreFile, trustStorePwd, trustStoreType);
			SecurityUtil.analyseCertificate(trustStore, List.of("client", "server"));
			trustStoreParameters.setKeyStore(trustStore);
			trustStoreParameters.setPassword(trustStorePwd);
			trustStoreParameters.setType(trustStoreType);
			trustManagersParameters.setKeyStore(trustStoreParameters);
			sslContext.setTrustManagers(trustManagersParameters);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private KeyStore readKeyStore(String keystoreFile, String keystorePwd, String keyStoreType) throws Exception {
		try (InputStream keyStoreStream = this.getClass().getClassLoader().getSystemResourceAsStream(keystoreFile)) {
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(keyStoreStream, keystorePwd.toCharArray());
			return keyStore;
		}
	}
	
}