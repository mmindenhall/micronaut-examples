package com.vendavo.cloud.data.model

import org.bson.codecs.pojo.annotations.BsonId

/**
 * Name is a data class for storing a simple Mongo type (_id and name fields).
 */
data class Name(
        @BsonId var _id: String? = null,
        var name: String? = null
)
