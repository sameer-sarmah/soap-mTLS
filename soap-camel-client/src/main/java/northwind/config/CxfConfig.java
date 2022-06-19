package northwind.config;

import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.BindingProvider;

import org.apache.camel.builder.component.ComponentsBuilderFactory;
import org.apache.cxf.bus.CXFBusFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.LoggingInInterceptor;
import org.apache.cxf.ext.logging.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduitConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import northwind.domain.customer.CustomersPort;
import northwind.util.KeyStoreUtil;
import northwind.util.SecurityUtil;

@Configuration
@ImportResource({ "classpath:META-INF/cxf/cxf.xml" })
@ComponentScan(basePackages = {"northwind"})
public class CxfConfig {

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
	
    @Bean
    public LoggingFeature loggingFeature() {
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        return loggingFeature;
    }

    @Bean(name = "customerServiceClient")
    public CustomersPort customerServiceClient(@Autowired LoggingFeature loggingFeature){
        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setServiceClass(CustomersPort.class);
        Map<String, Object> properties = factoryBean.getProperties();
        if(properties == null){
            properties = new HashMap<>();
        }

        properties.put("javax.xml.ws.client.connectionTimeout", 5000);
        properties.put("javax.xml.ws.client.receiveTimeout", 3000);

        factoryBean.setProperties(properties);

        factoryBean.getFeatures().add(loggingFeature);

        KeyStore keyStore;
		try {
			keyStore = readKeyStore(keyStoreFile,keyStorePwd,keyStoreType);
			SecurityUtil.analyseKeystore(keyStore,List.of("client","server"),"client");
		    KeyStore trustStore = readKeyStore(trustStoreFile,trustStorePwd,trustStoreType);
		    SecurityUtil.analyseCertificate(trustStore, List.of("client","server"));
	        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
	        keyManagerFactory.init(keyStore, keyStorePwd.toCharArray());
	        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

	        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        trustManagerFactory.init(trustStore);
	        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
	        
	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(keyManagers, trustManagers, null);
	        factoryBean.setBus(new CXFBusFactory().createBus());
	        factoryBean.getBus().setExtension((name, address, httpConduit) -> {
	            TLSClientParameters tls = new TLSClientParameters();
	            tls.setSSLSocketFactory(sslContext.getSocketFactory());
	            tls.setHostnameVerifier((hostname, sslSession) -> {
	                if (hostname.equals("localhost")) {
	                    return true;
	                }
	                return false;
	            });
	            httpConduit.setTlsClientParameters(tls);
	        }, HTTPConduitConfigurer.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

        CustomersPort theService = (CustomersPort) factoryBean.create();
        BindingProvider bindingProvider = (BindingProvider) theService;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://localhost:8443/service");
        return theService;
    }
    
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
	
	
	private KeyStore readKeyStore(String keystoreFile,String keystorePwd,String keyStoreType) throws Exception {
		try (InputStream keyStoreStream = this.getClass().getClassLoader().getSystemResourceAsStream(keystoreFile)) {
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(keyStoreStream, keystorePwd.toCharArray());
			return keyStore;
		}
	}
	
    @Bean
    public LoggingInInterceptor loggingInInterceptor() {
    	LoggingInInterceptor  loggingInInterceptor = new LoggingInInterceptor ();
		return loggingInInterceptor;
    } 
    
    @Bean
    public LoggingOutInterceptor loggingOutInterceptor() {
    	LoggingOutInterceptor  loggingOutInterceptor = new LoggingOutInterceptor ();
		return loggingOutInterceptor;
    }
    
}
