package repositories

import models.dto.Page

import scala.concurrent.Future

/**
  * Created by adildramdan on 11/17/17.
  */
trait BaseRepository[T] {

  def insert(o: T): Future[T]
  def update(id: Long, o: T): Future[T]
  def delete(id: Long): Future[Boolean]
  def page(page: Int = 0, size: Int = 10, sort: String = "asc", sortBy: String = "name", filter: String = ""): Future[Page[T]]
  def findById(id: Long): Future[Option[T]]
  def count: Future[Int]
}
