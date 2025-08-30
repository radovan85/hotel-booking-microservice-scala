package com.radovan.play.repositories.impl

import com.radovan.play.entity.NoteEntity
import com.radovan.play.repositories.NoteRepository
import jakarta.inject.{Inject, Singleton}
import jakarta.persistence.criteria.{CriteriaBuilder, CriteriaQuery, Predicate, Root}
import org.hibernate.{Session, SessionFactory}

import scala.jdk.CollectionConverters._

@Singleton
class NoteRepositoryImpl extends NoteRepository{

  private var sessionFactory: SessionFactory = _

  @Inject
  private def initialize(sessionFactory: SessionFactory): Unit = {
    this.sessionFactory = sessionFactory
  }

  private def withSession[T](block: Session => T): T = {
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

  override def save(noteEntity: NoteEntity): NoteEntity = {
    withSession { session =>
      if (noteEntity.getNoteId() == null) {
        session.persist(noteEntity)
      } else {
        session.merge(noteEntity)
      }
      session.flush()
      noteEntity
    }
  }

  override def findById(noteId: Integer): Option[NoteEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[NoteEntity] = cb.createQuery(classOf[NoteEntity])
      val root: Root[NoteEntity] = cq.from(classOf[NoteEntity])
      val predicates: Array[Predicate] = Array(cb.equal(root.get("noteId"), noteId))
      cq.where(predicates: _*)
      val results = session.createQuery(cq).getResultList.asScala.toList
      results.headOption
    }
  }

  override def deleteById(noteId: Integer): Unit = {
    withSession { session =>
      val noteEntity = session.get(classOf[NoteEntity], noteId)
      if (noteEntity != null) {
        session.remove(noteEntity)
      }
    }
  }

  override def findAll: Array[NoteEntity] = {
    withSession { session =>
      val cb: CriteriaBuilder = session.getCriteriaBuilder
      val cq: CriteriaQuery[NoteEntity] = cb.createQuery(classOf[NoteEntity])
      val root: Root[NoteEntity] = cq.from(classOf[NoteEntity])
      cq.select(root)
      session.createQuery(cq).getResultList.asScala.toArray
    }
  }

  override def deleteAll(): Unit = {
    withSession { session =>
      val hql = "DELETE FROM NoteEntity"
      session.createMutationQuery(hql).executeUpdate()
    }
  }



}
