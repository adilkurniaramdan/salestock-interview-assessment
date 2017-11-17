package repositories.reference

import javax.inject.Inject

import models.dto.Page
import models.entities.reference.Product
import repositories.BaseRepository

import scala.concurrent.Future.{successful => future}
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by adildramdan on 11/17/17.
  */
trait ProductRepository extends BaseRepository[models.entities.reference.Product]{
}

class ProductRepositoryMemory @Inject()()(implicit ec: ExecutionContext) extends ProductRepository {
  protected[this] var storage = scala.collection.mutable.Map[Long, Product]()

  override def insert(o: Product): Future[Product] = future{
    val id        = o.id.getOrElse(generateId())
    val toBeSaved = o.copy(id = Option(id))
    storage += (id -> toBeSaved)
    toBeSaved
  }

  override def update(id: Long, o: Product): Future[Product] = future{
    storage(id) = o
    o
  }

  override def delete(id: Long): Future[Boolean] = future{
    storage -= id
    true
  }

  override def page(page: Int, size: Int, sort: String, sortBy: String, filter: String): Future[Page[Product]] = future{
    val from  = (page - 1) * size
    val to    = from + size - 1
    val data  = storage.values.toSeq
      .filter(_.name.contains(filter.toLowerCase))
      .sortWith(generateSort(sort))
      .slice(from, to)
      .toList
    Page(data, page, size, sort, sortBy, data.size, filter)
  }

  override def findById(id: Long): Future[Option[Product]] = future{
    storage.get(id)
  }

  override def count: Future[Int] = future{
    storage.size
  }

  private def generateId() =
    storage.keys.max + 1

  private def generateSort(sort: String) = {
    def asc : (Product, Product) => Boolean = (p1, p2) => p1.id.get > p2.id.get
    def desc: (Product, Product) => Boolean = (p1, p2) => p1.id.get > p2.id.get
    if(sort == "asc") asc else desc
  }

}
