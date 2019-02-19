package com.vendavo.cloud.data.model

import com.fasterxml.jackson.annotation.JsonInclude

const val STATUS_SUCCESS = "success"
const val STATUS_FAIL = "fail"
const val STATUS_ERROR = "error"

/**
 * Response models a JSend (http://labs.omniti.com/labs/jsend) response from an API request.
 */
data class Response @JvmOverloads constructor(
        val status: String,
        @JsonInclude(value= JsonInclude.Include.NON_NULL) val message: String?,
        @JsonInclude(value= JsonInclude.Include.NON_NULL) val code: Int? = null,
        @JsonInclude(value= JsonInclude.Include.NON_NULL) val data: Any? = null
) {
    companion object Factory {
        @JvmStatic
        fun success(data: Any?): Response = Response(STATUS_SUCCESS, null, null, data)
        @JvmStatic
        fun fail(data: Any?): Response = Response(STATUS_FAIL, null, null, data)
        @JvmStatic @JvmOverloads
        fun error(message: String, code: Int? = null, data: Any? = null): Response =
                Response(STATUS_ERROR, message, code, data)
    }
}