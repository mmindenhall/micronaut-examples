package example

import com.mongodb.MongoClient
import com.vendavo.cloud.data.model.Name
import com.vendavo.cloud.data.rest.HelloController
import groovy.util.logging.Slf4j
import io.micronaut.context.annotation.Value
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.test.annotation.MicronautTest
import org.bson.Document
import org.bson.types.ObjectId
import spock.lang.Specification

import javax.inject.Inject

import static io.micronaut.http.HttpRequest.*

@Slf4j
@MicronautTest
class HelloControllerSpec extends Specification {

    @Inject
    MongoClient mongoClient

    @Inject
    @Value("\${micronaut.application.name}")
    String appName

    @Inject
    @Value("\${mongodb.db-name}")
    String dbName

    @Inject
    @Value("\${mongodb.collection-name}")
    String collectionName

    @Inject
    EmbeddedServer server

    @Inject
    @Client("/")
    HttpClient client

    def cleanup() {
        // delete all documents in the collection after every test
        def db = mongoClient.getDatabase(dbName)
        def col = db.getCollection(collectionName)
        col.deleteMany(new Document())
    }

    def "validate injected beans are not null"(){
        given: "HelloController"
        HelloController controller = server.applicationContext.getBean(HelloController)

        expect: "validate bean injection"
        controller.mongoClient != null
        controller.appName != null
        controller.dbName != null
        controller.collName != null
        controller.col != null
    }

    def "can create with POST, then retrieve by name, retrieve by ObjectId, and delete"() {
        given: "a name created via POST /${appName}/names"
        HttpResponse<Name> postResp = client.toBlocking().exchange(
                POST("/${appName}/names", new Name(null, aName)), Name.class)

        assert postResp.code() == 201
        assert postResp.body() != null
        assert postResp.body().name == aName
        assert ObjectId.isValid(postResp.body()._id)

        when: "the name is retrieved via GET /${appName}/hello/{name}"
        HttpResponse<String> getResp1 = client.toBlocking().exchange(
                GET("/${appName}/hello/${aName}"), String.class)

        then: "the correct response code and result are returned"
        getResp1.code() == 200
        getResp1.body() != null
        getResp1.body() == "Hello $aName!"


        when: "the name is retrieved via GET /${appName}/hello/{ObjectId}"
        HttpResponse<String> getResp2 = client.toBlocking().exchange(
                GET("/${appName}/hello/${postResp.body()._id}"), String.class)

        then: "the correct response code and result are returned"
        getResp2.code() == 200
        getResp2.body() != null
        getResp2.body() == "Hello $aName!"

        when: "the name is deleted via DELETE /${appName}/bye/{name}"
        HttpResponse<String> delResp = client.toBlocking().exchange(
                DELETE("/${appName}/bye/${aName}"), String.class)

        then: "the correct response code and result are returned"
        delResp.code() == 200
        delResp.body() != null
        delResp.body() == "Buh-bye then $aName!"

        when: "the name has been deleted and GET /${appName}/hello/{name} is called"
        HttpResponse<String> getResp3 = client.toBlocking().exchange(
                GET("/${appName}/hello/${aName}"), String.class)

        then: "404 Not Found is returned"
        def e = thrown(HttpClientResponseException)
        e.status.code == 404

        when: "the name has been deleted and GET /${appName}/hello/{ObjectId} is called"
        HttpResponse<String> getResp4 = client.toBlocking().exchange(
                GET("/${appName}/hello/${postResp.body()._id}"), String.class)

        then: "404 Not Found is returned"
        def e2 = thrown(HttpClientResponseException)
        e2.status.code == 404

        where:
        aName  | _
        "foo"  | _
        "bar"  | _
        "bat"  | _
        "baz"  | _
        "dude" | _
    }


    def "can delete by ObjectId"() {
        given: "a name created via POST /${appName}/names"
        def aName = "foo"
        HttpResponse<Name> postResp = client.toBlocking().exchange(
                POST("/${appName}/names", new Name(null, aName)), Name.class)

        assert postResp.code() == 201
        assert postResp.body() != null
        assert postResp.body().name == aName
        assert ObjectId.isValid(postResp.body()._id)

        when: "the name is deleted via DELETE /${appName}/bye/{ObjectId}"
        HttpResponse<String> delResp = client.toBlocking().exchange(
                DELETE("/${appName}/bye/${postResp.body()._id}"), String.class)

        then: "the correct response code and result are returned"
        delResp.code() == 200
        delResp.body() != null
        delResp.body() == "Buh-bye then $aName!"

        when: "the name has been deleted and GET /${appName}/hello/{name} is called"
        client.toBlocking().exchange(
                GET("/${appName}/hello/${aName}"), String.class)

        then: "404 Not Found is returned"
        def e = thrown(HttpClientResponseException)
        e.status.code == 404

        when: "the name has been deleted and GET /${appName}/hello/{ObjectId} is called"
        client.toBlocking().exchange(
                GET("/${appName}/hello/${postResp.body()._id}"), String.class)

        then: "404 Not Found is returned"
        def e2 = thrown(HttpClientResponseException)
        e2.status.code == 404
    }


    def "can create many with POST, then retrieve all via GET /hello"() {
        given: "several names created via POST /${appName}/names"
        def names = ["foo", "bar", "bat", "baz", "dude"]
        names.each {
            HttpResponse<Name> postResp = client.toBlocking().exchange(
                    POST("/${appName}/names", new Name(null, it)), Name.class)

            assert postResp.code() == 201
            assert postResp.body() != null
            assert postResp.body().name == it
            assert ObjectId.isValid(postResp.body()._id)
        }

        when: "the names are retrieved via GET /${appName}/hello"
        HttpResponse<String> getResp1 = client.toBlocking().exchange(
                GET("/${appName}/hello"), String.class)

        then: "the correct response code is returned, and contains each name"
        getResp1.code() == 200
        getResp1.body() != null
        getResp1.body().startsWith("Hello ")
        getResp1.body().endsWith("!")
        names.each { getResp1.body().contains(it) }
    }

    def "when name exists, POST responds with 303 See Other"() {
        given: "a name created via POST /${appName}/names"
        def n = "dude"
        HttpResponse<Name> postResp = client.toBlocking().exchange(
                POST("/${appName}/names", new Name(null, n)), Name.class)

        assert postResp.code() == 201
        assert postResp.body() != null
        assert postResp.body().name == n
        assert ObjectId.isValid(postResp.body()._id)
        log.info("\n\n\n ===== 1 \n\n\n")
        when: "the same name is used in another POST"
        HttpResponse<Name> postResp2 = client.toBlocking().exchange(
                POST("/${appName}/names", new Name(null, n)), Name.class)
        log.info("\n\n\n ===== 2 \n\n\n")

        then: "303 See Other is returned with the correct Location header"
        postResp2.code() == 303
        postResp2.body() == null
        postResp2.header("Location").endsWith("/hello/${n}")
    }

    def "deleting an object with invalid id returns 404"() {
        given:
        def invalidId = UUID.randomUUID().toString()

        when: "invalid id is passed to delete"
        client.toBlocking().exchange(
                DELETE("/${appName}/bye/${invalidId}"), String.class)

        then: "404 Not Found is thrown"
        def e2 = thrown(HttpClientResponseException)
        e2.status.code == 404
        e2.message.contains("- not found")
    }

    def "deleting an object with non-existent valid id returns 404"(){
        given:
        def objectId = new ObjectId().toString()

        when: "invalid id is passed to delete"
        client.toBlocking().exchange(
                DELETE("/${appName}/bye/${objectId}"), String.class)

        then: "404 Not Found is thrown"
        def e2 = thrown(HttpClientResponseException)
        e2.status.code == 404
        e2.message.contains("nobody found with id")
    }

    def "fetching with no name records throws 404" () {
        when: "invalid id is passed to delete"
        client.toBlocking().exchange(
                GET("/${appName}/hello"), String.class)

        then: "404 Not Found is thrown"
        def e2 = thrown(HttpClientResponseException)
        e2.status.code == 404
    }
}
