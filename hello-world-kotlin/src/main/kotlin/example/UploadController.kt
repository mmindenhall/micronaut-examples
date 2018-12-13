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

        var chunkNum = 0

        val upload: Single<Result<Boolean>> = Flowable.fromPublisher(dataFile)
                .subscribeOn(Schedulers.io())
                .map<Result<Boolean>> { p: PartData ->
                    val inStr = p.inputStream
                    try {
                        val n = IOUtils.copyLarge(inStr, out)
                        if (backoff(chunkNum)) {
                            logger.info { "===== copied chunk #${chunkNum}, size $n bytes" }
                        }
                        chunkNum++
                        Result.Success(true)
                    } finally {
                        inStr.close()
                    }
                }
                .reduce {_, cur -> cur }
                .doOnComplete { logger.info { "===== onComplete was invoked" } }
                .doOnError { logger.info { "===== onError was invoked" } }
                .onErrorReturn { t -> Result.Error(t) }
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