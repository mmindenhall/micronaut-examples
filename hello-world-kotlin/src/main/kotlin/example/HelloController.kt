package com.vendavo.cloud.data.rest

import com.mongodb.MongoClient
import com.mongodb.client.MongoCollection
import com.vendavo.cloud.data.model.Name
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.created
import io.micronaut.http.HttpResponse.notFound
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.HttpResponse.seeOther
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import mu.KLogging
import org.bson.types.ObjectId
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.findOneAndDelete
import org.litote.kmongo.findOneById
import org.litote.kmongo.getCollection
import org.litote.kmongo.set
import org.litote.kmongo.upsert
import java.net.URI
import javax.annotation.PostConstruct
import javax.inject.Inject

@Controller("/\${micronaut.application.name}")
class HelloController {

    companion object: KLogging()

    @Inject
    lateinit var mongoClient: MongoClient

    @Value("\${micronaut.application.name}")
    lateinit var appName: String

    @Value("\${mongodb.db-name}")
    lateinit var dbName: String

    @Value("\${mongodb.collection-name}")
    lateinit var collName: String

    lateinit var col : MongoCollection<Name>

    @PostConstruct
    fun createCollection() {
        val db = mongoClient.getDatabase(dbName)
        col = db.getCollection<Name>(collName)
    }

    @Get("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    fun sayHelloToAll(): HttpResponse<String> {
        logger.info { "$appName Received HTTP GET /hello" }

        val names = col.find().limit(1000).toList().map { it.name }

        return if (names.isEmpty()) {
            notFound("Nobody found to tell hello!")
        } else {
            ok("Hello ${names.joinToString()}!")
        }
    }

    @Get("/hello/{nameOrId}")
    @Produces(MediaType.TEXT_PLAIN)
    fun sayHelloToMyLittleFriend(nameOrId: String): HttpResponse<String> {
        logger.info { "$appName Received HTTP GET /hello/$nameOrId" }

        if (ObjectId.isValid(nameOrId)) {
            val name = col.findOneById(ObjectId(nameOrId))
            return if (name != null) {
                ok("Hello ${name.name}!")
            } else {
                notFound("Can't say hello - nobody found with id: $nameOrId!")
            }

        } else {
            val name = col.findOne("{name: '$nameOrId'}")
            return if (name != null) {
                ok("Hello ${name.name}!")
            } else {
                notFound("Can't say hello to $nameOrId - not found!")
            }
        }
    }


    @Delete("/bye/{nameOrId}")
    @Produces(MediaType.TEXT_PLAIN)
    fun buhBye(nameOrId: String): HttpResponse<String> {
        logger.info { "$appName Received HTTP DELETE /hello/$nameOrId" }

        if (ObjectId.isValid(nameOrId)) {
            val result = col.findOneAndDelete(Name::_id eq ObjectId(nameOrId))
            return if (result != null) {
                ok("Buh-bye then ${result.name}!")
            } else {
                notFound("Can't delete - nobody found with id: $nameOrId!")
            }

        } else {
            val result = col.findOneAndDelete("{name: '$nameOrId'}")
            return if (result != null) {
                ok("Buh-bye then $nameOrId!")
            } else {
                notFound("Can't delete $nameOrId - not found!")
            }
        }
    }

    @Post("/names")
    fun saveName(@Body name: Name): HttpResponse<Name> {
        logger.info { "$appName Received HTTP POST /name with $name" }

        val result = col.updateOne(Name::name eq name.name, set(Name::name, name.name), upsert())

        return if (result.upsertedId != null) {
            val retName = col.findOne(Name::name eq name.name)
            created(retName)
        } else {
            seeOther(URI("/hello/${name.name}"))
        }
    }
}
