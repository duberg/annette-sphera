package annette.core.json

import java.util.concurrent.TimeUnit

import io.circe.{ Decoder, DecodingFailure, FailedCursor, HCursor }
import io.circe.Decoder.{ withReattempt }
import io.circe.generic.AutoDerivation

import scala.concurrent.duration.{ Duration, FiniteDuration }

//import io.circe.generic._
//import io.circe.syntax._

trait Codec extends AutoDerivation
  // ExceptionMapping
  // with ProductCodec
  with FiniteDurationCodec // with ModelCodec
  with ExceptionCodec //with OperationCodec
  {

  //  implicit val encodeCreateRunnableInfo: Encoder[CreateRunnableInfo] = {
  //    case x: CreateProjectInfo => x.asJson
  //    case x: CreateProcessInfo => x.asJson
  //    case x: CreateTaskInfo => x.asJson
  //  }

  //  val m =  ModifyAttr(Modifier(UUID.randomUUID()))
  //
  //
  //  val m1 = m.asJson
  //
  //
  //  val x = TaskInfo(
  //    id = ActorId("gfhfg"),
  //    name = "",
  //    description = None,
  //    taskType = TaskType.SubProcessTask,
  //    status = RunnableStatus.Created,
  //    initiator = Initiator.ProcessManager,
  //    ownerId = UUID.randomUUID(),
  //    assigneeId = None,
  //    reviewerId = None,
  //    watcherIds = Set.empty,
  //    parentId = None,
  //    childIds = Set.empty,
  //    activeChildIds = Set.empty,
  //    processId = None,
  //    subProcessId = None,
  //    operationId = None,
  //    operationBpmnId = None,
  //    formTemplateId = None,
  //    startedBy = None,
  //    startedOn = None,
  //    actualEnd = None,
  //    plannedStart = None,
  //    plannedDuration = None,
  //    modifyAttr = m).asJson
  //
  //
  //  import io.circe.parser._
  //
  //  val xx = x.as[TaskInfo]
  //

  //val x1 = x.asJson.as[TaskInfo]

  //  /**
  //   * @group Decoding
  //   */
  //  implicit def decodeOption[A](implicit d: Decoder[A]): Decoder[Option[A]] = withReattempt {
  //    case c: HCursor =>
  //      println("================1")
  //      println(c.value)
  //      if (c.value.isNull) Right(None) else d(c) match {
  //        case Right(a) => Right(Some(a))
  //        case Left(df) => Left(df)
  //      }
  //    case c: FailedCursor =>
  //      if (!c.incorrectFocus) Right(None) else Left(DecodingFailure("[A]Option[A]", c.history))
  //  }
}
