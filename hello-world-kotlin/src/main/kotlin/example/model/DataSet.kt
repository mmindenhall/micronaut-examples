package example.model

import com.fasterxml.jackson.annotation.JsonInclude
//import org.bson.codecs.pojo.annotations.BsonId

/**
 * DataSet models the metadata provided when a data file is received.
 */
data class DataSet @JvmOverloads constructor(
//        @field:BsonId
//        var _id: String? = null,

        var dataSourceId: String? = null,

        @field:JsonInclude(value= JsonInclude.Include.NON_EMPTY)
        var stateId: String? = null,

        var mode: String? = null,

        var metadata: Map<String, Any> = mutableMapOf(),

        var path: String? = null,

        @field:JsonInclude(value= JsonInclude.Include.NON_EMPTY)
        var tenantId: String? = null
)
