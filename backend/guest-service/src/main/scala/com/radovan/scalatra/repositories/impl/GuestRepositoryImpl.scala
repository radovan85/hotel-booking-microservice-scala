package com.radovan.scalatra.repositories.impl

import com.radovan.scalatra.entity.GuestEntity
import com.radovan.scalatra.repositories.GuestRepository
import com.radovan.scalatra.services.PrometheusService
import jakarta.inject.{Inject, Singleton}
import jakarta.persistence.criteria.{CriteriaBuilder, CriteriaQuery, Predicate, Root}
import org.hibernate.{Session, SessionFactory}

import scala.jdk.CollectionConverters._

@Singleton
class GuestRepositoryImpl extends GuestRepository{

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


  override def findById(guestId: Integer): Option[GuestEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[GuestEntity] = cb.createQuery(classOf[GuestEntity])
      val root: Root[GuestEntity] = cq.from(classOf[GuestEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("guestId"), guestId))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }

  override def findByUserId(userId: Integer): Option[GuestEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[GuestEntity] = cb.createQuery(classOf[GuestEntity])
      val root: Root[GuestEntity] = cq.from(classOf[GuestEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("userId"), userId))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }

  override def save(guestEntity: GuestEntity): GuestEntity = {
    withSession { session =>
      if (guestEntity.getGuestId() != null) {
        session.merge(guestEntity)
      } else {
        session.persist(guestEntity)
      }
      session.flush()
      guestEntity
    }
  }

  override def deleteById(guestId: Integer): Unit = {
    withSession { session =>
      val guestEntity = session.get(classOf[GuestEntity], guestId)
      if (guestEntity != null) session.remove(guestEntity)
    }
  }

  override def findAll: Array[GuestEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[GuestEntity] = cb.createQuery(classOf[GuestEntity])
      val root: Root[GuestEntity] = cq.from(classOf[GuestEntity])
      cq.select(root)

      session.createQuery(cq).getResultList.asScala.toArray
    }
  }
}
