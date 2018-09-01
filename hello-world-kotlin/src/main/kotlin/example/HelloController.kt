/*
 * Copyright 2017 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example

import com.mongodb.MongoClient
import io.micronaut.context.annotation.Value
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import mu.KLogging
import javax.inject.Inject
import javax.inject.Named

/**
 * @author James Kleeh
 * @since 1.0
 */
@Controller("/")
class HelloController {

    companion object: KLogging()

    @Inject
    private lateinit var mongoClient: MongoClient

    @Value("\${mongodb.db}")
    private lateinit  var dbName: String

    @Value("\${mongodb.collection}")
    private lateinit var collection: String

    @Get("/hello/{name}")
    fun hello(name: String): String {

        logger.info { "===== using injected mongoClient" }
        val db = mongoClient.getDatabase(dbName)
        val col = db.getCollection(collection)
        logger.info { "===== collection ${col.namespace.collectionName} has ${col.countDocuments()} documents" }

        return "Hello $name"
    }
}