package models.dto

import models.dto.reference.{CouponDto, ProductDto}
import models.entities.reference.{Coupon, Product}
import utils.MapperFormat

/**
  * Created by adildramdan on 11/19/17.
  */
trait DtoMapperFormats {
  implicit object mapProductToProductDto extends MapperFormat[Product, ProductDto]{
    override def map(t: Product): ProductDto =
      ProductDto(
        id          = t.id,
        name        = t.name,
        description = t.description,
        qty         = t.qty,
        unitPrice   = t.unitPrice
      )
  }

  implicit object mapCouponToCouponDto extends MapperFormat[Coupon, CouponDto]{
    override def map(t: Coupon): CouponDto =
      CouponDto(
        id          = t.id,
        code        = t.code,
        name        = t.name,
        description = t.description,
        amount      = t.amount,
        qty         = t.qty,
        rate        = t.rate,
        start       = t.start,
        end         = t.end
      )
  }
}

object DtoMapperFormats extends DtoMapperFormats
