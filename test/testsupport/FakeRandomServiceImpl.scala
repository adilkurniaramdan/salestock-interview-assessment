package testsupport

import services.application.RandomService

/**
  * Created by adildramdan on 11/19/17.
  */
class FakeRandomServiceImpl(value: String) extends RandomService {
  def randomAlphaNumericString(length: Int): String = {
    value
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