package example

import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import io.micronaut.configuration.mongo.reactive.DefaultMongoClientFactory
import io.micronaut.configuration.mongo.reactive.DefaultMongoConfiguration
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requirements
import io.micronaut.context.annotation.Requires
import mu.KLogging
import org.litote.kmongo.KMongo
import javax.inject.Singleton

/**
 * Replaces Micronaut's DefaultMongoClientFactory with a factory to produce a MongoClient created by
 * KMongo (https://litote.org/kmongo/).
 */
@Requirements(Requires(beans = [DefaultMongoConfiguration::class]), Requires(classes = [MongoClient::class]))
@Replaces(factory = DefaultMongoClientFactory::class)
@Factory
class KMongoClientFactory {

    companion object: KLogging()

    @Bean(preDestroy = "close")
    @Singleton
    fun kmongoClient(configuration: DefaultMongoConfiguration): MongoClient {
        logger.debug { "===== KMongo creating client for ${configuration.uri}" }
        val client = KMongo.createClient(MongoClientURI(configuration.uri))
        return client
    }
}
