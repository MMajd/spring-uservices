package mmajd.microservices.core.review;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
@ComponentScan("mmajd")
public class ReviewServiceApplication {
  private static final Logger LOG = LoggerFactory.getLogger(ReviewServiceApplication.class);

  private final Integer threadPoolSize;
  private final Integer taskQueueSize;

  @Autowired
  public ReviewServiceApplication(
          @Value("${app.threadPoolSize:10}") Integer threadPoolSize,
          @Value("${app.taskQueueSize:100}") Integer taskQueueSize
  ) {
    this.threadPoolSize = threadPoolSize;
    this.taskQueueSize = taskQueueSize;
  }


  /** JDBC doesn't support reactor, so use reactor Scheduler
   * We could use R2DBC, but just giving example on how to support non-reactive code
   */
  @Bean
  public Scheduler jdbcScheduler() {
    LOG.info("Creates a jdbc scheduler with thread pool of size {}, and queue with size {}", threadPoolSize, taskQueueSize);
    return Schedulers.newBoundedElastic(threadPoolSize, taskQueueSize, "jdbc-pool");
  }


  public static void main(String[] args) {
    ConfigurableApplicationContext context =
            SpringApplication.run(ReviewServiceApplication.class, args);

    String mysqlUri = context.getEnvironment().getProperty("spring.datasource.url");
    LOG.info("Connected to MySQL: {}", mysqlUri);
  }
}
