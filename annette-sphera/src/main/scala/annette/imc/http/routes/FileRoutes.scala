package annette.imc.http.routes

import java.io.FileOutputStream
import java.util.UUID
import akka.http.scaladsl.model.Multipart.FormData.BodyPart
import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.pattern.ask
import akka.util.{ ByteString, Timeout }
import annette.imc.ApsActor._
import annette.imc.model.ApFile._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.model.headers._
import scala.concurrent.Future
import annette.core.exception.AnnetteException
import annette.imc.model.{ ApStatus, _ }
import annette.imc.notification._
import annette.imc.user.model._
import scala.util.{ Failure, Success }
import annette.imc.utils.Implicits._
import annette.imc.utils.Files
import annette.core.utils.Generator

trait FileRoutes
  extends NotificationConfig
  with Generator { self: APIContext with API =>

  private def processFile(filePath: String, fileData: Multipart.FormData) = {
    val fileOutput = new FileOutputStream(filePath)

    def writeFileOnLocal(array: Array[Byte], byteString: ByteString): Array[Byte] = {
      val byteArray: Array[Byte] = byteString.toArray
      fileOutput.write(byteArray)
      array ++ byteArray
    }

    fileData.parts.mapAsync(1) {
      bodyPart: BodyPart =>
        for {
          x <- bodyPart.entity.dataBytes.runFold(Array[Byte]())(writeFileOnLocal)
          y <- Future(bodyPart.filename)
        } yield (x, y.getOrElse("Undefined"))

    }.runFold((0, ""))((a, b) => (a._1 + b._1.length, b._2))
  }

  private val upload = (path("upload" / JavaUUID) & post & entity(as[Multipart.FormData]) & auth) { (apId, fileData, sessionData) =>
    val userId = sessionData.userId
    val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

    val ff: Future[Unit] = for {

      u <- userFuture
      _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
      a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
      _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))
    } yield {}

    onComplete(ff) {
      case Success(_) => complete {

        val fileName = UUID.randomUUID()
        val fileStorageDir = ctx.fileStorageDir
        val filePath = fileStorageDir + "/" + fileName.toString
        processFile(filePath, fileData).map { fileMeta =>
          val apFile = ApFile(fileName, fileMeta._2, "Ru", FileType(FileType.OtherFiles))
          apsActor ! AddFileCmd(apId, apFile)
          HttpResponse(StatusCodes.OK, entity = apFile.asJson.toString())
        }.recover {
          case ex: Exception => HttpResponse(StatusCodes.InternalServerError, entity = "Error in file uploading")
        }
      }

      case Failure(throwable) =>
        throwable match {
          case annetteException: AnnetteException =>
            complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
          case _ =>
            complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
        }
    }

  }

  private val download = (path("download" / JavaUUID / JavaUUID) & get & auth) {
    (apId, fileId, sessionData) =>
      val file = ctx.fileStorageDir + "/" + fileId
      Files.downloadFile(file)

  }

  private val update = (path("update" / JavaUUID) & post & auth & entity(as[ApFile])) {
    (apId, sessionData, file) =>
      {
        val userId = sessionData.userId
        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
          _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))
          f <- apsActor.ask(UpdateFileCmd(apId, file))

        } yield f

        onComplete(ff) {
          case Success(Done) => complete("Done")
          case Success(FileNotExists) => complete("FileNotExists")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(_) => complete(StatusCodes.InternalServerError)
          case Failure(throwable) =>
            throwable match {
              case annetteException: AnnetteException =>
                complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
              case _ =>
                complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
            }
        }
      }
  }

  private val getFile = (path("get" / JavaUUID / JavaUUID) & get & auth) {
    (apId, fileId, sessionData) =>
      {
        val userId = sessionData.userId
        val f = apsActor.ask(GetApById(apId))
        onComplete(f) {
          case Success(ApFound(x)) =>
            x.apFiles.get(fileId) match {
              case Some(y) => complete(y.asJson)
              case None => complete("FileNotExists")
            }

          case Success(_) => complete(StatusCodes.InternalServerError)
          case Failure(throwable) =>
            throwable match {
              case annetteException: AnnetteException =>
                complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
              case _ =>
                complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
            }
        }
      }
  }

  private val remove = (path("remove" / JavaUUID / JavaUUID) & get & auth) {
    (apId, fileId, sessionData) =>
      {

        val userId = sessionData.userId
        val userFuture = getUserRoled(userId).mapTo[Option[UserRoled]]

        val ff = for {

          u <- userFuture
          _ <- predicate(u.exists(_.manager))(new Exception("must be in role of manager"))
          a <- apsActor.ask(GetApById(apId)).mapTo[ApFound]
          _ <- predicate(a.ap.apStatus.nameMessage == ApStatus.FILLING)(new Exception("must be filling"))

          f <- apsActor.ask(RemoveFileCmd(apId, fileId))
        } yield f

        onComplete(ff) {

          case Success(Done) => {
            val file = ctx.fileStorageDir + "/" + fileId
            deleteFile(file)
            complete("Done")
          }
          case Success(FileNotExists) => complete("FileNotExists")
          case Success(ApNotExists) => complete("ApNotExists")
          case Success(_) => complete(StatusCodes.InternalServerError)
          case Failure(throwable) =>
            throwable match {
              case annetteException: AnnetteException =>
                complete(StatusCodes.InternalServerError -> annetteException.exceptionMessage)
              case _ =>
                complete(StatusCodes.InternalServerError -> Map("code" -> throwable.getMessage))
            }
        }
      }
  }

  def fileRoutes = pathPrefix("file") {

    upload ~ download ~ update ~ getFile ~ remove
  }

}
