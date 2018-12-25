package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import service.ParserWriter;

@SpringBootApplication
@ComponentScan(basePackages = "service")
@PropertySource("classpath:application.properties")
public class Application {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
        context.getBean(ParserWriter.class).parseAndWrite();
    }
}
