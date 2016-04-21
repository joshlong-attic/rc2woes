package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.stream.Stream;

@EnableBinding (Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}


@MessageEndpoint
class ReservationProcessor {

	@Autowired
	private ReservationRepository reservationRepository ;

	@ServiceActivator (inputChannel =  "input")
	public void acceptNewReservationsPlease (String rn) {
	 this.reservationRepository.save(new Reservation(rn));
	}

}

@RestController
@RefreshScope
class MessageRestContorller  {

	@Value("${message}")
	private String message;

	@RequestMapping(method = RequestMethod.GET, value = "/message")
	String read() {
		return this.message;
	}
}

@Component
class DummyCLR implements CommandLineRunner {
	@Autowired
	private ReservationRepository reservationRepository;

	@Override
	public void run(String... args) throws Exception {
		Stream.of("Juergen", "Oliver", "Christoph", "Martin", "Janitor", "Josh")
				.forEach(n -> reservationRepository.save(new Reservation(n)));
		reservationRepository.findAll().forEach(System.out::println);
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

}

@Entity
class Reservation {

	@Id
	@GeneratedValue
	private Long id;
	private String reservationName;

	public Long getId() {
		return id;
	}

	@Override
	public String toString() {
		return "Reservation{" +
				"id=" + id +
				", reservationName='" + reservationName + '\'' +
				'}';
	}

	Reservation() {// why JPA why?
	}

	public Reservation(String reservationName) {

		this.reservationName = reservationName;
	}

	public String getReservationName() {
		return reservationName;
	}
}