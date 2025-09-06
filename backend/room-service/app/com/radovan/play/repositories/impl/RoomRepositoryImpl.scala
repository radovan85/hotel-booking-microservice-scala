package com.radovan.play.repositories.impl

import com.radovan.play.entity.RoomEntity
import com.radovan.play.repositories.RoomRepository
import com.radovan.play.services.PrometheusService
import jakarta.inject.{Inject, Singleton}
import jakarta.persistence.criteria.{CriteriaBuilder, CriteriaQuery, Predicate, Root}
import org.hibernate.{Session, SessionFactory}

import scala.jdk.CollectionConverters._

@Singleton
class RoomRepositoryImpl extends RoomRepository{

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

  override def findById(roomId: Integer): Option[RoomEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[RoomEntity] = cb.createQuery(classOf[RoomEntity])
      val root: Root[RoomEntity] = cq.from(classOf[RoomEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("roomId"), roomId))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }

  override def save(roomEntity: RoomEntity): RoomEntity = {
    withSession { session =>
      if (roomEntity.getRoomId() == null) {
        session.persist(roomEntity)
      } else {
        session.merge(roomEntity)
      }
      session.flush()
      roomEntity
    }
  }

  override def deleteById(roomId: Integer): Unit = {
    withSession { session =>
      val room = session.get(classOf[RoomEntity], roomId)
      if (room != null) {
        session.remove(room)
      }
    }
  }

  override def findAll: Array[RoomEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[RoomEntity] = cb.createQuery(classOf[RoomEntity])
      val root: Root[RoomEntity] = cq.from(classOf[RoomEntity])
      cq.select(root)
      session.createQuery(cq).getResultList.asScala.toArray
    }
  }

  override def findAllByCategoryId(categoryId: Integer): Array[RoomEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[RoomEntity] = cb.createQuery(classOf[RoomEntity])
      val root: Root[RoomEntity] = cq.from(classOf[RoomEntity])

      val predicate: Predicate = cb.equal(root.get("roomCategory").get("roomCategoryId"), categoryId)
      cq.where(Array(predicate): _*)

      val query = session.createQuery(cq)
      query.getResultList.asScala.toArray
    }
  }

  override def findByRoomNumber(roomNumber: Integer): Option[RoomEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[RoomEntity] = cb.createQuery(classOf[RoomEntity])
      val root: Root[RoomEntity] = cq.from(classOf[RoomEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("roomNumber"), roomNumber))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }
}
