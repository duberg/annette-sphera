/** *************************************************************************************
  * Copyright (c) 2014-2017 by Valery Lobachev
  * Redistribution and use in source and binary forms, with or without
  * modification, are NOT permitted without written permission from Valery Lobachev.
  *
  * Copyright (c) 2014-2017 Валерий Лобачев
  * Распространение и/или использование в исходном или бинарном формате, с изменениями или без таковых,
  * запрещено без письменного разрешения правообладателя.
  * ***************************************************************************************/
package annette.core.services.authentication.jwt

import annette.core.services.authentication.SessionData
import io.igl.jwt._

import scala.util.Success

trait JwtHelper {

  val secret: String

  def decodeSessionData(token: String): Option[SessionData] = {
    val decodedTry = DecodedJwt.validateEncodedJwt(
      token, // An encoded jwt as a string
      secret, // The key to validate the signature against
      Algorithm.HS256, // The algorithm we require
      Set(Typ), // The set of headers we require (excluding alg)
      Set(Sid, Aid, Uid, Tid, Lid), // The set of claims we require
    )
    //println(s"decodedTry = $decodedTry")
    decodedTry match {
      case Success(decoded) =>
        for {
          sid <- decoded.getClaim[Sid]
          uid <- decoded.getClaim[Uid]
          tid <- decoded.getClaim[Tid]
          aid <- decoded.getClaim[Aid]
          lid <- decoded.getClaim[Lid]
        } yield SessionData(
          sid.value,
          uid.value,
          tid.value,
          aid.value,
          lid.value
        )
      case _ =>
        None
    }
  }

  def encodeSessionData(sessionData: SessionData): String = {
    val data = Seq(
      Sid(sessionData.sessionId),
      Uid(sessionData.userId),
      Tid(sessionData.tenantId),
      Aid(sessionData.applicationId),
      Lid(sessionData.languageId)
    )
    val jwt = new DecodedJwt(Seq(Alg(Algorithm.HS256), Typ("JWT")), data)
    jwt.encodedAndSigned(secret)
  }

}

