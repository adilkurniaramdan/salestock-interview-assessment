package utils

/**
  * Created by adildramdan on 11/19/17.
  */

trait MapperFormat[T, U] {
  def map(t: T) : U
}

trait Mapper {
  def map[T,U](s: T)(implicit r: MapperFormat[T, U]): U =
    r.map(s)
}
object Mapper extends Mapper

