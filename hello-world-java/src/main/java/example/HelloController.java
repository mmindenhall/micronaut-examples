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
package example;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import io.micronaut.context.annotation.Value;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.validation.Validated;
import io.reactivex.Single;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotBlank;

/**
 * @author Graeme Rocher
 * @since 1.0
 */
@Controller("/")
@Validated
public class HelloController {
    private static final Logger log = LoggerFactory.getLogger(HelloController.class);

    @Inject @Named("myMongoClient") MongoClient mongoClient;

    @Value("${mongodb.db}") String dbName;
    @Value("${mongodb.collection}") String collection;

    @Get("/hello/{name}")
    public String hello(@NotBlank String name) {
        log.info("===== using injected mongoClient");

        MongoDatabase db = mongoClient.getDatabase(dbName);
        MongoCollection col = db.getCollection(collection);

        log.info("===== collection {} has {} documents", col.getNamespace().getCollectionName(), col.countDocuments());

        return "Hello " + name + "!";
    }
}
