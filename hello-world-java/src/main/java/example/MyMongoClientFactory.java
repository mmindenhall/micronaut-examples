package example;

import com.mongodb.MongoClient;
import io.micronaut.configuration.mongo.reactive.DefaultMongoConfiguration;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

@Requires(classes = MongoClient.class)
@Requires(beans = DefaultMongoConfiguration.class)
@Factory
public class MyMongoClientFactory {
    private static final Logger log = LoggerFactory.getLogger(MyMongoClientFactory.class);

    @Bean(preDestroy = "close")
    @Singleton
    @Named("myMongoClient")
    public MongoClient myMongoClient(DefaultMongoConfiguration configuration) {
        log.info("===== creating myMongoClient");
        return new MongoClient(configuration.buildURI());
    }
}
