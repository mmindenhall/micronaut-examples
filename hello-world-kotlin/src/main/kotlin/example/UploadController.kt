package example

import example.model.DataSet
import example.model.Response
import example.model.Result
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Produces
import io.micronaut.http.multipart.PartData
import io.micronaut.http.multipart.StreamingFileUpload
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import mu.KLogging
import org.apache.commons.io.IOUtils
import java.io.File
import java.io.FileOutputStream

@Controller("/upload")
class UploadController {

    companion object: KLogging()

    @Post("/")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    fun postSet(dataSet: DataSet, dataFile: StreamingFileUpload): HttpResponse<Response> {
        logger.info { "===== posted DataSet: $dataSet" }

        val tempFile = File.createTempFile(dataFile.filename, ".temp")
        val out = FileOutputStream(tempFile)
        logger.info { "===== attempting to write uploaded file to ${tempFile.absolutePath}" }

        val upload: Single<Result<Boolean>> = Flowable.fromPublisher(dataFile)
                .subscribeOn(Schedulers.io())
                .map<Result<Boolean>> { p: PartData ->
                    // original code fixed to call p.InputStream just once per @jameskleeh
//                    val inStr = p.inputStream
//                    try {
//                        val n = IOUtils.copyLarge(inStr, out)
//                        logger.info { "===== copied chunk of $n bytes" }
//                        Result.Success(true)
//                    } finally {
//                        inStr.close()
//                    }

                    // alternative using p.bytes
                    val bytes = p.bytes
                    IOUtils.write(bytes, out)
                    logger.info { "===== copied chunk of ${bytes.size} bytes, starts with:${if (bytes.size > 0) String(bytes.sliceArray(0..29)).replace("\n", "\\n") else ""}" }
                    Result.Success(true)
                }
                .onErrorReturn { t -> Result.Error(t) }
                .reduce {_, _ -> Result.Success(true) }
                .toSingle()

        // wait for upload to complete
        logger.info { "===== Executing upload.blockingGet()" }
        val uploadResult = upload.blockingGet()
        when (uploadResult) {
            is Result.Success -> {
                logger.info { "===== File uploaded" }
                return HttpResponse.created(Response.success(dataSet))
            }
            is Result.Error -> {
                logger.error("Upload failed: ", uploadResult.throwable)
                return HttpResponse.badRequest(Response.fail(mapOf("error" to uploadResult.throwable.message)))
            }
        }
    }

}