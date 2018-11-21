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
                    try {
                        val bytes = p.bytes
                        IOUtils.write(bytes, out)
//                        val n = IOUtils.copyLarge(p.inputStream, out)
//                        logger.info { "===== copied chunk of $n bytes, starts with:\n${String(p.bytes.sliceArray(0..200))}" }
                        logger.info { "===== copied chunk of ${bytes.size} bytes, starts with:\n${String(bytes.sliceArray(0..59)).replace("\n", "\\n")}" }
                        Result.Success(true)
                    } finally {
                        p.inputStream.close()
                    }
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