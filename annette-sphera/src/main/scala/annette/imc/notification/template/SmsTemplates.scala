package annette.imc.notification.template

/**
 * Шаблоны смс сообщений.
 *
 * todo: Нужно поместить все это в хранилице и добавить динамику,
 * чтобы пользователь мог редактировать шаблоны
 */
object SmsTemplates {
  val Link = "https://imc-msk.com"

  def subject(language: String): String = language match {
    case "EN" => EN.Subject
    case _ => RU.Subject
  }

  def password(language: String, password: String): String = language match {
    case "EN" => EN.password(password)
    case _ => RU.password(password)
  }

  def verification(language: String, code: String): String = language match {
    case "EN" => EN.verification(code)
    case _ => RU.verification(code)
  }

  def toExpertise(language: String): String = language match {
    case "EN" => EN.toExpertise
    case _ => RU.toExpertise
  }

  def toReview(language: String): String = language match {
    case "EN" => EN.toReview
    case _ => RU.toReview
  }

  object EN {
    val Subject = "MIMC"

    def password(password: String): String =
      s"""You were successfully registered in the system of Moscow International Medical Cluster $Link
         |Your password: $password""".stripMargin

    def verification(code: String): String =
      s"Moscow International Medical Cluster, verification code: $code"

    def toExpertise: String =
      s"""Dear expert! There is a new application in your personal account $Link
         |Please fill the bulletin in 2 weeks""".stripMargin

    def toReview: String =
      s"Dear expert! There is a new application for review in your personal account $Link"
  }

  object RU {
    val Subject = "ММК"

    def password(password: String): String =
      s"""Вы были зарегистрированы в системе Международного медицинского кластера $Link
         |Ваш постоянный пароль: $password""".stripMargin

    def verification(code: String): String =
      s"Международный медицинский кластер, код подтверждения: $code"

    def toExpertise: String =
      s"""Уважаемый эксперт! В вашем личном кабинете появилась новая заявка для проведения экспертизы $Link
         |Пожалуйста, заполните бюллетень в течении двух недель""".stripMargin

    def toReview: String =
      s"В вашем личном кабинете появилась новая заявка для рассмотрения $Link"
  }
}