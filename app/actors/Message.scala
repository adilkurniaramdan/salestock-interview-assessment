package actors

/**
  * Created by adildramdan on 11/19/17.
  */

sealed trait Message

trait Command extends Message

trait Event extends Message

trait Query extends Message

trait Response extends Message
