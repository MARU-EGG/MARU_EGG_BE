package mju.iphak.maru_egg;

import org.springframework.boot.SpringApplication;

public class TestMaruEggApplication {

	public static void main(String[] args) {
		SpringApplication.from(MaruEggApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
