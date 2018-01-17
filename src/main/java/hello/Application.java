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
