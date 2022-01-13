package debug;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class TestApplication {

  public static void main(final String[] args) {
    SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(TestApplication.class);
    appBuilder.build().run(args);
  }
}
