package example

import com.mongodb.MongoClient
import io.micronaut.configuration.mongo.reactive.DefaultMongoConfiguration
import io.micronaut.context.annotation.*
import mu.KLogging
import javax.inject.Named
import javax.inject.Singleton

@Requirements(
        Requires(classes = arrayOf(MongoClient::class)),
        Requires(beans = arrayOf(DefaultMongoConfiguration::class)))
@Factory
class MyMongoClientFactory {

    companion object: KLogging()

    @Bean(preDestroy = "close")
    @Singleton
    @Replaces(MongoClient::class)
    fun myMongoClient(configuration: DefaultMongoConfiguration): MongoClient {
        logger.info { "===== creating myMongoClient" }
        return MongoClient(configuration.buildURI())
    }
}