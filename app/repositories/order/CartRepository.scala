package repositories.order

import javax.inject.Inject

import models.dto.Page
import repositories.BaseRepository

import scala.concurrent.Future.{successful => future}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by adildramdan on 11/17/17.
  */
trait CartRepository extends BaseRepository[models.entities.reference.Cart]{
}

class CartRepositoryMemory @Inject()()(implicit ec: ExecutionContext) extends CartRepository {
  protected[this] var storage = scala.collection.mutable.Map[Long, Cart]()

  override def insert(o: Cart): Future[Cart] = future{
    val id        = o.id.getOrElse(generateId())
    val toBeSaved = o.copy(id = Option(id))
    storage += (id -> toBeSaved)
    toBeSaved
  }

  override def update(id: Long, o: Cart): Future[Cart] = future{
    storage(id) = o
    o
  }

  override def delete(id: Long): Future[Boolean] = future{
    storage -= id
    true
  }

  override def page(page: Int, size: Int, sort: String, sortBy: String, filter: String): Future[Page[Cart]] = future{
    val from  = (page - 1) * size
    val to    = from + size - 1
    val data  = storage.values.toSeq
      .filter(_.name.contains(filter.toLowerCase))
      .sortWith(generateSort(sort))
      .slice(from, to)
      .toList
    Page(data, page, size, sort, sortBy, data.size, filter)
  }

  override def findById(id: Long): Future[Option[Cart]] = future{
    storage.get(id)
  }

  override def count: Future[Int] = future{
    storage.size
  }

  private def generateId() =
    storage.keys.max + 1

  private def generateSort(sort: String) = {
    def asc : (Cart, Cart) => Boolean = (p1, p2) => p1.id.get > p2.id.get
    def desc: (Cart, Cart) => Boolean = (p1, p2) => p1.id.get > p2.id.get
    if(sort == "asc") asc else desc
  }

}
