package com.radovan.play.modules

import com.google.inject.AbstractModule
import com.radovan.play.brokers.{ReservationNatsListener, ReservationNatsSender}
import com.radovan.play.converter.TempConverter
import com.radovan.play.repositories.impl.{NoteRepositoryImpl, ReservationRepositoryImpl}
import com.radovan.play.repositories.{NoteRepository, ReservationRepository}
import com.radovan.play.services.impl.{MoleculerRegistrationServiceImpl, MoleculerServiceDiscoveryImpl, NoteServiceImpl, PrometheusServiceImpl, ReservationServiceImpl}
import com.radovan.play.services.{MoleculerRegistrationService, MoleculerServiceDiscovery, NoteService, PrometheusService, ReservationService}
import com.radovan.play.utils.{JwtUtil, NatsUtils, PublicKeyCache, ServiceUrlProvider}
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}


class AutoBindModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[NoteService]).to(classOf[NoteServiceImpl]).asEagerSingleton()
    bind(classOf[ReservationService]).to(classOf[ReservationServiceImpl]).asEagerSingleton()
    bind(classOf[MoleculerRegistrationService]).to(classOf[MoleculerRegistrationServiceImpl]).asEagerSingleton()
    bind(classOf[MoleculerServiceDiscovery]).to(classOf[MoleculerServiceDiscoveryImpl]).asEagerSingleton()
    bind(classOf[PrometheusService]).to(classOf[PrometheusServiceImpl]).asEagerSingleton()
    bind(classOf[NoteRepository]).to(classOf[NoteRepositoryImpl]).asEagerSingleton()
    bind(classOf[ReservationRepository]).to(classOf[ReservationRepositoryImpl]).asEagerSingleton()
    bind(classOf[TempConverter]).asEagerSingleton()
    bind(classOf[JwtUtil]).asEagerSingleton()
    bind(classOf[NatsUtils]).asEagerSingleton()
    bind(classOf[PublicKeyCache]).asEagerSingleton()
    bind(classOf[ServiceUrlProvider]).asEagerSingleton()
    bind(classOf[ReservationNatsSender]).asEagerSingleton()
    bind(classOf[ReservationNatsListener]).asEagerSingleton()

    val prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    bind(classOf[PrometheusMeterRegistry]).toInstance(prometheusRegistry)
    bind(classOf[MeterRegistry]).toInstance(prometheusRegistry)

  }
}