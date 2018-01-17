# Build Soap Web Service Client

## Generate domain objects based on a WSDL
The interface to a SOAP web service is captured in a WSDL. JAXB provides an easy means to generate Java classes from a WSDL (or rather: the XSD contained in the <Types/> section of the WSDL). The WSDL for the quote service can be found at http://localhost:8080/ws. We can save it as movies.wsdl under src/main/resources

To generate Java classes from the WSDL in Gradle, we will need the following in the build file:

``` shell
task genJaxb {
    ext.sourcesDir = "${buildDir}/generated-sources/jaxb"
    ext.classesDir = "${buildDir}/classes/jaxb"
    ext.schema = "src/main/resources/movies.wsdl"

    outputs.dir classesDir

    doLast() {
        project.ant {
            taskdef name: "xjc", classname: "com.sun.tools.xjc.XJCTask",
                    classpath: configurations.jaxb.asPath
            mkdir(dir: sourcesDir)
            mkdir(dir: classesDir)

            xjc(destdir: sourcesDir, schema: schema,
                    package: "hello.wsdl") {
                arg(value: "-wsdl")
                produces(dir: sourcesDir, includes: "**/*.java")
            }

            javac(destdir: classesDir, source: 1.8, target: 1.8, debug: true,
                    debugLevel: "lines,vars,source",
                    classpath: configurations.jaxb.asPath) {
                src(path: sourcesDir)
                include(name: "**/*.java")
                include(name: "*.java")
            }

            copy(todir: classesDir) {
                fileset(dir: sourcesDir, erroronmissingdir: false) {
                    exclude(name: "**/*.java")
                }
            }
        }
    }
}
```

## Create a movie service client

To create a web service client, we simply have to extend the __WebServiceGatewaySupport__ class and code the operations:

__src/main/java/hello/MovieClient.java__

``` java
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
```

The __WebServiceTemplate__ supplied by the __WebServiceGatewaySupport__ base class handles the actual SOAP exchange. It passes the __GetMovieRequest__ request object and casts the response into a __GetMovieResponse__ object, which is then returned.

## Configuring web service components

Spring WS uses Spring Framework’s OXM module which has the Jaxb2Marshaller to serialize and deserialize XML requests.

__src/main/java/hello/MovieConfiguration.java__

``` java
package hello;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class MovieConfiguration {
	@Bean
	public Jaxb2Marshaller marshaller() {
		Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
		
		// this package must match the package in the <generatedPackage> specified in gradle.build
		marshaller.setContextPath("hello.wsdl");
		return marshaller;
	}
	
	@Bean
	public MovieClient movieClient(Jaxb2Marshaller marshaller) {
		MovieClient client = new MovieClient();
		client.setDefaultUri("http://localhost:8080/ws");
		client.setMarshaller(marshaller);
		client.setUnmarshaller(marshaller);
		
		return client;
	}

}
```

The marshaller is pointed at the collection of generated domain objects and will use them to both serialize and deserialize between XML and POJOs.

The movieClient is created and configured with the URI of the movie service. It is also configured to use the JAXB marshaller.

## Make the application executable

This application is packaged up to run from the console and retrieve the movie information for a given title.

__src/main/java/hello/Application.java__

``` java
package hello;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import hello.wsdl.GetMovieResponse;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner lookup(MovieClient client) {
		return args -> {
			String name = "Spectre";
			
			if (args.length > 1) {
				name = args[1];
			}
			GetMovieResponse response = client.getMovie(name);
			System.err.println(response.getMovie().getDirector());
		};
	}
}
```
