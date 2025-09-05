package com.radovan.play.repositories.impl

import com.radovan.play.entity.ReservationEntity
import com.radovan.play.repositories.ReservationRepository
import com.radovan.play.services.PrometheusService
import jakarta.inject.{Inject, Singleton}
import jakarta.persistence.criteria.{CriteriaBuilder, CriteriaQuery, Predicate, Root}
import org.hibernate.{Session, SessionFactory}

import scala.jdk.CollectionConverters._

@Singleton
class ReservationRepositoryImpl extends ReservationRepository {

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

  override def findAllByRoomId(roomId: Integer): Array[ReservationEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[ReservationEntity] = cb.createQuery(classOf[ReservationEntity])
      val root: Root[ReservationEntity] = cq.from(classOf[ReservationEntity])

      val predicate: Predicate = cb.equal(root.get("roomId"), roomId)
      cq.where(Array(predicate): _*)

      val query = session.createQuery(cq)
      query.getResultList.asScala.toArray
    }
  }

  override def save(reservationEntity: ReservationEntity): ReservationEntity = {
    withSession { session =>
      if (reservationEntity.getReservationId() == null) {
        session.persist(reservationEntity)
      } else {
        session.merge(reservationEntity)
      }
      session.flush()
      reservationEntity
    }
  }


  override def findAll: Array[ReservationEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[ReservationEntity] = cb.createQuery(classOf[ReservationEntity])
      val root: Root[ReservationEntity] = cq.from(classOf[ReservationEntity])
      cq.select(root)
      session.createQuery(cq).getResultList.asScala.toArray
    }
  }

  override def findAllByGuestId(guestId: Integer): Array[ReservationEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[ReservationEntity] = cb.createQuery(classOf[ReservationEntity])
      val root: Root[ReservationEntity] = cq.from(classOf[ReservationEntity])

      val predicate: Predicate = cb.equal(root.get("guestId"), guestId)
      cq.where(Array(predicate): _*)

      val query = session.createQuery(cq)
      query.getResultList.asScala.toArray
    }
  }

  override def deleteById(reservationId: Integer): Unit = {
    withSession { session =>
      val reservationEntity = session.get(classOf[ReservationEntity], reservationId)
      if (reservationEntity != null) {
        session.remove(reservationEntity)
      }
    }
  }

  override def findById(reservationId: Integer): Option[ReservationEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[ReservationEntity] = cb.createQuery(classOf[ReservationEntity])
      val root: Root[ReservationEntity] = cq.from(classOf[ReservationEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("reservationId"), reservationId))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }

  override def deleteAllByGuestId(guestId: Integer): Unit = {
    withSession { session =>
      val hql = "DELETE FROM ReservationEntity WHERE guestId = :guestId"
      session.createMutationQuery(hql)
        .setParameter("guestId", guestId)
        .executeUpdate()
    }
  }


  override def deleteAllByRoomId(roomId: Integer): Unit = {
    withSession { session =>
      val hql = "DELETE FROM ReservationEntity WHERE roomId = :roomId"
      session.createMutationQuery(hql)
        .setParameter("roomId", roomId)
        .executeUpdate()
    }
  }


}
