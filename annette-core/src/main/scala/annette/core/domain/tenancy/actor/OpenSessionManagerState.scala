package annette.core.domain.tenancy.actor

import annette.core.akkaext.actor.CqrsState
import annette.core.domain.tenancy.OpenSessionManager
import annette.core.domain.tenancy.OpenSessionManager._
import annette.core.domain.tenancy.model.{ OpenSession, OpenSessionUpdate }

case class OpenSessionManagerState(openSessions: Map[OpenSession.Id, OpenSession] = Map.empty) extends CqrsState {

  def createOpenSession(entry: OpenSession): OpenSessionManagerState = {
    if (openSessions.get(entry.id).isDefined) throw new IllegalArgumentException
    else copy(openSessions = openSessions + (entry.id -> entry))
  }

  def updateOpenSession(entry: OpenSessionUpdate): OpenSessionManagerState = {
    openSessions
      .get(entry.id)
      .map {
        e =>
          val updatedEntry = e.copy(
            applicationId = entry.applicationId.getOrElse(e.applicationId),
            languageId = entry.languageId.getOrElse(e.languageId),
            rememberMe = entry.rememberMe.getOrElse(e.rememberMe),
            tenantId = entry.tenantId.getOrElse(e.tenantId),
            lastOpTimestamp = entry.lastOpTimestamp.getOrElse(e.lastOpTimestamp))
          copy(openSessions = openSessions + (entry.id -> updatedEntry))
      }
      .getOrElse(throw new IllegalArgumentException)
  }

  def deleteOpenSession(id: OpenSession.Id): OpenSessionManagerState = {
    if (openSessions.get(id).isEmpty) throw new IllegalArgumentException
    else copy(openSessions = openSessions - id)
  }

  def findOpenSessionById(id: OpenSession.Id): Option[OpenSession] = openSessions.get(id)

  def findAllOpenSessions: Seq[OpenSession] = openSessions.values.toSeq

  def openSessionExists(id: OpenSession.Id): Boolean = openSessions.get(id).isDefined

  def update: Update = {
    case OpenSessionCreatedEvt(entry) => createOpenSession(entry)
    case OpenSessionUpdatedEvt(entry) => updateOpenSession(entry)
    case OpenSessionDeletedEvt(id) => deleteOpenSession(id)
  }
}
