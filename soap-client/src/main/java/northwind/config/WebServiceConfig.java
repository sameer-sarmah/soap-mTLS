package northwind.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.transport.http.HttpsUrlConnectionMessageSender;

import northwind.util.SecurityUtil;
import northwind.ws.client.SoapClient;

@ComponentScan(basePackages = {"northwind"})
@Configuration
public class WebServiceConfig {

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
	
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	  @Bean
	  public Jaxb2Marshaller marshaller() {
	    Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
	    // this package must match the package in the <generatePackage> specified in pom.xml
	    marshaller.setContextPath("northwind.domain.customer.wsdl");
	    return marshaller;
	  }

	  @Bean
	  public SoapClient soapClient(Jaxb2Marshaller marshaller) throws Exception{
		 SoapClient client = new SoapClient();
	    client.setDefaultUri("https://localhost:8443/service/customers.wsdl");
	    client.setMarshaller(marshaller);
	    client.setUnmarshaller(marshaller);
	    KeyStore keyStore = readKeyStore(keyStoreFile,keyStorePwd,keyStoreType);
		SecurityUtil.analyseKeystore(keyStore,List.of("client","server"),"client");
	    KeyStore trustStore = readKeyStore(trustStoreFile,trustStorePwd,trustStoreType);
	    SecurityUtil.analyseCertificate(trustStore, List.of("client","server"));
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, keyStorePwd.toCharArray());


        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        HttpsUrlConnectionMessageSender messageSender = new HttpsUrlConnectionMessageSender();
        messageSender.setKeyManagers(keyManagerFactory.getKeyManagers());
        messageSender.setTrustManagers(trustManagerFactory.getTrustManagers());

        // otherwise: java.security.cert.CertificateException: No name matching localhost found
        messageSender.setHostnameVerifier((hostname, sslSession) -> {
            if (hostname.equals("localhost")) {
                return true;
            }
            return false;
        });
        client.setMessageSender(messageSender);
	    return client;
	  }
	  
		private KeyStore readKeyStore(String keystoreFile,String keystorePwd,String keyStoreType) throws Exception {
			try (InputStream keyStoreStream = this.getClass().getClassLoader().getSystemResourceAsStream(keystoreFile)) {
				KeyStore keyStore = KeyStore.getInstance(keyStoreType);
				keyStore.load(keyStoreStream, keystorePwd.toCharArray());
				return keyStore;
			}
		}
		


}
