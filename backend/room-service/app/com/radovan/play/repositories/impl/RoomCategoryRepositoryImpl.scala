package com.radovan.play.repositories.impl

import com.radovan.play.entity.RoomCategoryEntity
import com.radovan.play.repositories.RoomCategoryRepository
import com.radovan.play.services.PrometheusService
import jakarta.inject.{Inject, Singleton}
import jakarta.persistence.criteria.{CriteriaBuilder, CriteriaQuery, Predicate, Root}
import org.hibernate.{Session, SessionFactory}

import scala.jdk.CollectionConverters._

@Singleton
class RoomCategoryRepositoryImpl extends RoomCategoryRepository{

  private var sessionFactory:SessionFactory = _
  private var prometheusService:PrometheusService = _

  @Inject
  private def initialize(sessionFactory: SessionFactory,prometheusService: PrometheusService):Unit = {
    this.sessionFactory = sessionFactory
    this.prometheusService = prometheusService
  }

  private def withSession[T](block: Session => T): T = {
    prometheusService.updateDatabaseQueryCount()
    val session = sessionFactory.openSession()
    val transaction = session.beginTransaction()

    try {
      val result = block(session)
      transaction.commit()
      result
    } catch {
      case e: Exception =>
        transaction.rollback()
        throw e
    } finally {
      session.close()
    }
  }


  override def findById(categoryId: Integer): Option[RoomCategoryEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[RoomCategoryEntity] = cb.createQuery(classOf[RoomCategoryEntity])
      val root: Root[RoomCategoryEntity] = cq.from(classOf[RoomCategoryEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("roomCategoryId"), categoryId))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }

  override def findByName(name: String): Option[RoomCategoryEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[RoomCategoryEntity] = cb.createQuery(classOf[RoomCategoryEntity])
      val root: Root[RoomCategoryEntity] = cq.from(classOf[RoomCategoryEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("name"), name))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }

  override def save(categoryEntity: RoomCategoryEntity): RoomCategoryEntity = {
    withSession { session =>
      if (categoryEntity.getRoomCategoryId() == null) {
        session.persist(categoryEntity)
      } else {
        session.merge(categoryEntity)
      }
      session.flush()
      categoryEntity
    }
  }

  override def findAll: Array[RoomCategoryEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[RoomCategoryEntity] = cb.createQuery(classOf[RoomCategoryEntity])
      val root: Root[RoomCategoryEntity] = cq.from(classOf[RoomCategoryEntity])
      cq.select(root)
      session.createQuery(cq).getResultList.asScala.toArray
    }
  }

  override def deleteById(categoryId: Integer): Unit = {
    withSession { session =>
      val categoryEntity = session.get(classOf[RoomCategoryEntity], categoryId)
      if (categoryEntity != null) {
        session.remove(categoryEntity)
      }
    }
  }
}
