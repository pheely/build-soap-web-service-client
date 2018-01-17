package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import hello.wsdl.GetMovieRequest;
import hello.wsdl.GetMovieResponse;

public class MovieClient extends WebServiceGatewaySupport {
	
	private static final Logger log = LoggerFactory.getLogger(MovieClient.class);
	
	public GetMovieResponse getMovie(String name) {
		GetMovieRequest request = new GetMovieRequest();
		request.setName(name);
		
		log.info("Requesting info for {}", name);
		
		GetMovieResponse response = (GetMovieResponse) getWebServiceTemplate().marshalSendAndReceive("http://localhost:8080/ws", request);
		
		return response;
	}

}
