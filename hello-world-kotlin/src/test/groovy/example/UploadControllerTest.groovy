package example

import com.fasterxml.jackson.databind.ObjectMapper
import example.model.DataSet
import example.model.Response
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.multipart.MultipartBody
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import static io.micronaut.http.HttpRequest.POST

class UploadControllerTest extends Specification {

    @Shared @AutoCleanup EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer)
    @Shared @AutoCleanup HttpClient client = HttpClient.create(embeddedServer.URL)

    @Shared ObjectMapper mapper = new ObjectMapper()
    @Shared def tenantId = UUID.randomUUID().toString()

    @Shared dataSetJson = """\
        {
            "dataSourceId": "dsId1",
            "mode": "FULL",
            "metadata": {
                "datatype": "com.vendavo.cloud.datatypes.Transaction"
            }
        }""".stripIndent()

    def "test very large file"() {
        given:
        File f = new File(getClass().getResource('/big.txt').toURI())

        MultipartBody requestBody = MultipartBody.builder()
                .addPart("dataSet", "dataSet.json", MediaType.APPLICATION_JSON_TYPE, dataSetJson.bytes)
                .addPart("dataFile", "transactions-001.avro", new MediaType("avro/binary", "avro"), f)
                .build()

        when:
        HttpResponse<Response> postResp = client.toBlocking().exchange(
                POST("/upload", requestBody)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON_TYPE)
                        .header("Tenant-Id", tenantId),
                Response.class)

        then:
        postResp.code() == HttpStatus.CREATED.code
        DataSet dSet = mapper.convertValue(postResp.body().data, DataSet.class)
        dSet.dataSourceId == "dsId1"
        dSet.mode == "FULL"
    }
}
