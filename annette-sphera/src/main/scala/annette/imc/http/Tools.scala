package annette.imc.http

import annette.core.domain.tenancy.model.User

import scala.concurrent.Future

object Tools {
  def generatePass: String = {
    val sounds = Seq("Nee", "Bo", "my", "cho", "vee", "Voo", "Na", "Jo", "Cea", "ha",
      "cy", "kea", "Va", "xea", "dy", "Boo", "Da", "So", "Ry", "joo",
      "ly", "woo", "vu", "Gy", "fi", "qea", "du", "hee", "Bi", "ni",
      "fee", "Ba", "lu", "noo", "Zu", "cha", "Ji", "vi", "Hu", "Ree",
      "di", "bu", "Fy", "Zoo", "goo", "ve", "Zi", "Shee", "Kee", "Xu",
      "de", "Hi", "Xee", "Fu", "Soo", "Choo", "Mea", "Sha", "Nu",
      "Dee", "Chi", "me", "Loo", "lee", "be", "Joo", "Dea", "Che",
      "shy", "jo", "ree", "Coo", "ca", "Ge", "ro", "gea", "ka",
      "fy", "rea", "Ga", "Co", "sa", "qoo", "Do", "Lo", "Ky",
      "wy", "chee", "Wa", "fo", "Jea", "Wea", "Sy", "hoo",
      "Mi", "Ma", "Lee", "wu", "gi", "chea", "Su", "mu", "Qy",
      "Moo", "xi", "Koo", "cu", "Si", "Qu", "Sho", "see",
      "xe", "Doo", "Gu", "fe", "hea", "ne", "Ri", "sea", "xa",
      "jee", "Je", "roo", "co", "na", "Re", "Cho", "Vee", "le",
      "ko", "va", "Ze", "Shea", "Kea", "He", "Bee", "so", "Go",
      "ry", "Vea", "la", "Mee", "zy", "Woo", "Za", "hy", "Ha",
      "Wo", "Vy", "Hee", "ba", "choo", "Dy", "Cha", "shu", "xy",
      "Ly", "xoo", "Mo", "Xa", "Hea", "Fi", "ri", "Fa", "By", "Ni", "gee",
      "Goo", "zi", "Lu", "xu", "chy", "hi", "shi", "fu", "Jy", "bee", "nu",
      "qi", "Ju", "bea", "soo", "Ru", "qe", "Wee", "chi", "Ci", "Nea", "Shy", "xo",
      "Lea", "ge", "Ki", "zee", "Ce", "no", "Xoo", "Rea", "ga", "we", "Ke", "vo", "do",
      "zea", "Qoo", "Ro", "Se", "qee", "lo", "Zo", "fea", "Ho", "sy", "Sa", "bo", "wee", "Hoo",
      "ma", "lea", "cee", "Wy", "jea", "Xo", "Ja", "Fo", "qy", "koo", "da", "Xea", "Ra", "My",
      "voo", "cea", "Wu", "si", "Zee", "shee", "qu", "boo", "Cy", "ru", "bi", "Fee", "zu", "See",
      "ji", "Chee", "Noo", "hu", "Xi", "Vu", "Fea", "Ku", "je", "Xe", "re", "Vi", "Roo", "Di",
      "zoo", "Bu", "ze", "vea", "mee", "chu", "Li", "she", "kee", "Shoo", "ra", "Ve", "xee",
      "De", "ny", "za", "mea", "sha", "Le", "wo", "Ko", "vy", "dee", "loo", "Be", "mo", "qa",
      "La", "dea", "Shu", "Zy", "che", "fa", "Hy", "coo", "by", "Qo", "Gee", "Ca", "jy",
      "Chy", "Shi", "Xy", "ky", "wi", "Ka", "wa", "shoo", "Gea", "li", "ju", "Ny", "She",
      "Chu", "mi", "ku", "Foo", "Bea", "su", "ci", "wea", "Qi", "nee", "Qa", "ki", "Du",
      "ce", "Gi", "Qe", "nea", "ke", "Mu", "moo", "gu", "se", "Wi", "Zea", "shea", "Qee",
      "Cu", "No", "zo", "sho", "gy", "foo", "ho", "Sea", "We", "Vo", "Chea", "doo",
      "Jee", "Qea", "Fe", "qo", "Me", "ja", "Cee", "Ne", "he", "go")
    val vovels = Seq("a", "e", "i", "o", "u", "y")
    val consonants = Seq("b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "x", "z", "w")

    def generateRandomSound(): String = {
      sounds(scala.util.Random.nextInt(sounds.size))
    }
    def generateRandomVovel(): String = {
      if (scala.util.Random.nextBoolean) vovels(scala.util.Random.nextInt(vovels.size))
      else ""
    }
    def generateRandomConsonant(): String = {
      if (scala.util.Random.nextBoolean) consonants(scala.util.Random.nextInt(consonants.size))
      else ""
    }
    generateRandomVovel + generateRandomSound +
      generateRandomSound + generateRandomSound +
      generateRandomConsonant
  }

  def buildFIO(u: User): String = s"${u.lastName} ${u.firstName.take(1)}.${
    u.middleName.getOrElse("") match {
      case x: String if x.isEmpty => ""
      case x: String => x.take(1) + "."
    }
  }"

  def buildFullName(u: User): String = s"${u.lastName} ${u.firstName}${
    u.middleName.getOrElse("") match {
      case x: String if x.isEmpty => ""
      case x: String => " " + x
    }
  }"

}
