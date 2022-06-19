package northwind.cxf.configurer;

import java.util.Objects;

import org.apache.camel.component.cxf.CxfConfigurer;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.AbstractWSDLBasedEndpointFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;

public class DisableCNVerificationConfigurer  implements CxfConfigurer {

	@Override
	public void configure(AbstractWSDLBasedEndpointFactory factoryBean) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureClient(Client client) {
		Conduit conduit =  client.getConduit();
	    if(Objects.nonNull(conduit) && conduit instanceof HTTPConduit) {
	    	HTTPConduit httpConduit = (HTTPConduit)conduit;
	        TLSClientParameters tls = Objects.nonNull(httpConduit.getTlsClientParameters()) ? httpConduit.getTlsClientParameters() : new TLSClientParameters();
	        tls.setHostnameVerifier((hostname, sslSession) -> {
	            if (hostname.equals("localhost")) {
	                return true;
	            }
	            return false;
	        });
	        tls.setDisableCNCheck(true);
	        httpConduit.setTlsClientParameters(tls);
	    }
	}

	@Override
	public void configureServer(Server server) {
		// TODO Auto-generated method stub
		
	}

}
