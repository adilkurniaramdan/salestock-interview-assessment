package services.application


/**
  * Created by adildramdan on 11/17/17.
  */

trait RandomService {

  def randomAlphaNumericString(length: Int): String

  def randomStringFromCharList(length: Int, chars: Seq[Char]): String
}

class RandomServiceImpl() extends RandomService {
  def randomAlphaNumericString(length: Int): String = {
    val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
    randomStringFromCharList(length, chars)
  }

  def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val randomNum = scala.util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }
}
