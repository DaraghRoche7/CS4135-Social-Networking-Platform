package ie.ul.studyhub.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class SupportServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(SupportServiceApplication.class, args);
  }
}

