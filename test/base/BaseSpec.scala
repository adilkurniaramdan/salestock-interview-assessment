package base

import org.scalatest.{Matchers, WordSpec}

/**
 * A good practice with Scala-test is to provide a base class for the tests in one project
 * instead of repeating the combination of spec base class and matchers for each test class.
 */
abstract class BaseSpec extends WordSpec   with Matchers
