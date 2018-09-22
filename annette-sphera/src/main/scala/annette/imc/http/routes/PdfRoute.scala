package annette.imc.http.routes

import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.server.Directives.{ entity, pathPrefix, _ }
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe._
import io.circe.generic.auto._
import io.circe.syntax._
import akka.http.scaladsl.model.headers._
import annette.core.exception.AnnetteException
import annette.imc.report.ReportService

import scala.util.{ Failure, Success }
import CacheDirectives._
import ContentDispositionTypes._
import annette.core.CoreModule
import annette.core.utils.Generator
import annette.imc.report.model.{ ReportFormatType, TemplateDataB }
import io.github.cloudify.scala.spdf._
import java.io._
import java.net._
import java.util.UUID

import annette.core.domain.tenancy.model.{ TenantUserRole, User }
import annette.imc.model.Ap
import annette.imc.user.model.{ FullUser, ImcUser }

import scala.concurrent.Future
import scala.xml.Elem

trait PdfRoute { self: APIContext with API =>

  def pdfRoutes: Route = {
    pathPrefix("pdf") {
      (path("draft" / JavaUUID) & get & auth) { (apId, sessionData) =>

        val html: Future[Elem] = for {
          allUsers: Set[User] <- getUsersAll
          allUserRole: Set[TenantUserRole] <- getUserRoleAll
          allImcUsers: Map[UUID, ImcUser] <- getAllImcUsers
          ap: Ap <- getApById(apId)
        } yield {

          val x = for (user <- allUsers) yield {
            val roles = allUserRole.find(_.userId == user.id).map(_.roles)
            val imcUser = allImcUsers.get(user.id)

            FullUser(
              user.id,
              user.lastname,
              user.firstname,
              user.middlename,
              imcUser.flatMap(_.company),
              imcUser.flatMap(_.position),
              imcUser.flatMap(_.rank),
              roles.exists(_.contains("admin")),
              roles.exists(_.contains("secretar")),
              roles.exists(_.contains("manager")),
              roles.exists(_.contains("chairman")),
              roles.exists(_.contains("expert")),
              roles.exists(_.contains("additional")))
          }

          val applicant = ap.apData.name.map(_.ru).getOrElse("")

          val chairman = x.find(_.chairman)
          val chairmanName = chairman.map(y => s"${y.lastname} ${y.firstname.take(1)}.${
            y.middlename match {
              case x: String if x == "" => ""
              case x: String => " " + x.take(1) + "."
            }
          }").getOrElse("")
          val secretar = x.find(_.secretar)
          val secretarName = secretar.map(y => s"${y.lastname} ${y.firstname.take(1)}.${
            y.middlename match {
              case x: String if x == "" => ""
              case x: String => " " + x.take(1) + "."
            }
          }").getOrElse("")

          val scoresM = ap.expertise.bulletins.values.filter(x => x.isFinished && !x.rejection.contains(true)).flatMap(_.scores.map(x => x.medical))
          val scoresE = ap.expertise.bulletins.values.filter(x => x.isFinished && !x.rejection.contains(true)).flatMap(_.scores.map(x => x.educational))
          val scoresS = ap.expertise.bulletins.values.filter(x => x.isFinished && !x.rejection.contains(true)).flatMap(_.scores.map(x => x.scientific))

          val experts = ap.expertise.bulletins.values.count(!_.rejection.contains(true))
          val votes = ap.expertise.bulletins.values.filter(x => x.isFinished && !x.rejection.contains(true))
          val notVoted = ap.expertise.bulletins.values.filter(x => !x.isFinished
            && !x.rejection.contains(true) && allUsers.map(_.id).contains(x.expertId))
          val rejectors = ap.expertise.bulletins.values.filter(x => x.rejection.contains(true)
            && allUsers.map(_.id).contains(x.expertId))

          val pluses = votes.count(_.finalResult.contains(true))
          val quorum: Boolean = if (experts > 0) votes.size.toFloat / experts.toFloat > 0.5 else false

          val correspond: Seq[Int] = (1 to 5).flatMap(i => votes.toSeq.map(_.criterions.get(i).count(_.decision == 2)))
          val notCorrespond = (1 to 5).flatMap(i => votes.toSeq.map(_.criterions.get(i).count(_.decision == 1)))
          val rejectorsList = rejectors.toList.map { bulletin =>

            x.find(_.id == bulletin.expertId).map(u => s"${u.lastname} ${u.firstname.take(1)}.${
              u.middlename match {
                case x: String if x == "" => ""
                case x: String => " " + x.take(1) + "."
              }
            }").getOrElse("")
          }

          val notVotedList: Seq[String] = notVoted.toList.map { bulletin =>

            x.find(_.id == bulletin.expertId).map(u => s"${u.lastname} ${u.firstname.take(1)}.${
              u.middlename match {
                case x: String if x == "" => ""
                case x: String => " " + x.take(1) + "."
              }
            }").getOrElse("")
          }
          val criterions = votes.toList.map { bulletin =>
            (
              x.find(_.id == bulletin.expertId).map(u => s"${u.lastname} ${u.firstname.take(1)}.${
                u.middlename match {
                  case x: String if x == "" => ""
                  case x: String => " " + x.take(1) + "."
                }
              }").getOrElse(""),
              bulletin.criterions.get(1).map(_.decision) match {
                case Some(2) => "Да"
                case Some(1) => "Нет"
                case _ => "—"
              },
              bulletin.criterions.get(2).map(_.decision) match {
                case Some(2) => "Да"
                case Some(1) => "Нет"
                case _ => "—"
              },
              bulletin.criterions.get(3).map(_.decision) match {
                case Some(2) => "Да"
                case Some(1) => "Нет"
                case _ => "—"
              },
              bulletin.criterions.get(4).map(_.decision) match {
                case Some(2) => "Да"
                case Some(1) => "Нет"
                case _ => "—"
              },
              bulletin.criterions.get(5).map(_.decision) match {
                case Some(2) => "Да"
                case Some(1) => "Нет"
                case _ => "—"
              },
              bulletin.finalResult match {
                case Some(true) => "Соответствует"
                case Some(false) => "Не соответствует"
                case _ => "—"
              })
          }
          val balls = votes.toList.map { bulletin =>
            (
              x.find(_.id == bulletin.expertId).map(u => s"${u.lastname} ${u.firstname.take(1)}.${
                u.middlename match {
                  case x: String if x == "" => ""
                  case x: String => " " + x.take(1) + "."
                }
              }").getOrElse(""),
              bulletin.scores.map(_.medical).getOrElse("—").toString,
              bulletin.scores.map(_.scientific).getOrElse("—").toString,
              bulletin.scores.map(_.educational).getOrElse("—").toString)
          }

          val average = (scoresM.size match {
            case x if x != 0 => Math.round(scoresM.sum * 100 / scoresM.size).toFloat / 100
            case 0 => 0
          },
            scoresS.size match {
              case x if x != 0 => Math.round(scoresS.sum * 100 / scoresS.size).toFloat / 100
              case 0 => 0
            },
            scoresE.size match {
              case x if x != 0 => Math.round(scoresE.sum * 100 / scoresE.size).toFloat / 100
              case 0 => 0
            })

          val result = if (!quorum) "Нет кворума"
          else if (pluses.toFloat / votes.size.toFloat >= 0.67) "Соответствует"
          else "Не соответствует"

          <html>
            <head>
              <meta charset="utf-8"/>
              <style>
                table{{border-collapse:collapse;width: 100%;}}
                table td,table th{{border:1px solid black;padding:10px;text-align:center;}}
                table.result td{{width:50%;}}
                table.voted {{margin-top:20px;}}
                table.balls td {{width: 25%;}}
                table.t th {{width: 10%;}}
                table.t th:first-of-type {{width: 25%;}}
                table.t th:last-of-type {{width: 25%;}}
                tr.bold td {{font-weight: bold;}}
              </style>
            </head>
            <body>
              <h2>{ applicant }</h2>
              <h3>Мнения Экспертов о соответствии заявки критериям отбора:</h3>
              <table class="crit t">
                <tr>
                  <th>Эксперт\Критерий</th>
                  <th>1</th>
                  <th>2</th>
                  <th>3</th>
                  <th>4</th>
                  <th>5</th>
                  <th>Итоговое мнение<br/> Эксперта</th>
                </tr>
                {
                  criterions.map(x =>
                    <tr>
                      <td>{ x._1 }</td>
                      <td>{ x._2 }</td>
                      <td>{ x._3 }</td>
                      <td>{ x._4 }</td>
                      <td>{ x._5 }</td>
                      <td>{ x._6 }</td>
                      <td>{ x._7 }</td>
                    </tr>)
                }
              </table>
              <table class="voted t">
                <tr>
                  <th>Проголосовало: { votes.size }</th>
                  <th>1</th>
                  <th>2</th>
                  <th>3</th>
                  <th>4</th>
                  <th>5</th>
                  <th>Итоговое мнение</th>
                </tr>
                <tr>
                  <td>Да</td>
                  <td>{ correspond.applyOrElse(0, (_: Int) => 0) }</td>
                  <td>{ correspond.applyOrElse(1, (_: Int) => 0) }</td>
                  <td>{ correspond.applyOrElse(2, (_: Int) => 0) }</td>
                  <td>{ correspond.applyOrElse(3, (_: Int) => 0) }</td>
                  <td>{ correspond.applyOrElse(4, (_: Int) => 0) }</td>
                  <td rowspan="2">{ result }</td>
                </tr>
                <tr>
                  <td>Нет</td>
                  <td>{ notCorrespond.applyOrElse(0, (_: Int) => 0) }</td>
                  <td>{ notCorrespond.applyOrElse(1, (_: Int) => 0) }</td>
                  <td>{ notCorrespond.applyOrElse(2, (_: Int) => 0) }</td>
                  <td>{ notCorrespond.applyOrElse(3, (_: Int) => 0) }</td>
                  <td>{ notCorrespond.applyOrElse(4, (_: Int) => 0) }</td>
                </tr>
              </table>
              <h3>Балльная оценка</h3>
              <table class="balls">
                <tr>
                  <th>Эксперт\Вид</th>
                  <th>Медицинская</th>
                  <th>Научная</th>
                  <th>Образовательная</th>
                </tr>
                {
                  balls.map(x =>
                    <tr>
                      <td>{ x._1 }</td>
                      <td>{ x._2 }</td>
                      <td>{ x._3 }</td>
                      <td>{ x._4 }</td>
                    </tr>)
                }
                <tr class="bold">
                  <td>Средний балл</td>
                  <td>{ average._1 }</td>
                  <td>{ average._2 }</td>
                  <td>{ average._3 }</td>
                </tr>
              </table>
              <h3>Итоговое мнение экспертного совета</h3>
              <table class="result">
                <tr class="bold">
                  <td>{ result }</td>
                  <td>Коэф. { if (votes.size.toFloat > 0.0) Math.round(pluses.toFloat * 100.0 / votes.size.toFloat) / 100.0 else 0 }</td>
                </tr>
              </table>
              <p>Справочно:</p>
              <p>Не закончили экспертизу: { if (notVotedList.size == 0) <strong>0</strong> }</p>
              { if (notVotedList.size != 0) <p> { notVotedList.mkString(", ") }</p> }
              <p>Отказались от экспертизы: { if (rejectorsList.size == 0) <strong>0</strong> }</p>
              { if (rejectorsList.size != 0) <p> { rejectorsList.mkString(", ") }</p> }
            </body>
          </html>
        }

        onComplete(html) {
          case Success(page: Elem) =>

            val pdf = WrappedPdf(Seq("xvfb-run", "wkhtmltopdf"), new PdfConfig {
              marginTop := "1cm"
              marginBottom := "1cm"
              marginLeft := "1cm"
              marginRight := "1cm"
            })

            val outputStream = new ByteArrayOutputStream
            pdf.run(page, outputStream)
            val bytes = outputStream.toByteArray
            complete(HttpResponse(
              entity = HttpEntity(MediaTypes.`application/pdf`, bytes),
              headers = List(
                `Cache-Control`(`no-cache`),
                `Content-Disposition`.apply(attachment, Map("filename" -> "draft.pdf")))))
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
  }
}

