package com.radovan.scalatra.modules

import com.google.inject.AbstractModule
import com.radovan.scalatra.services.{ApiGatewayService, MoleculerRegistrationService, MoleculerServiceDiscovery, PrometheusService}
import com.radovan.scalatra.services.impl.{ApiGatewayServiceImpl, MoleculerRegistrationServiceImpl, MoleculerServiceDiscoveryImpl, PrometheusServiceImpl}
import com.radovan.scalatra.utils.ServiceUrlProvider
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.{PrometheusConfig, PrometheusMeterRegistry}
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext


class AutoBindModule extends AbstractModule {

  override def configure(): Unit = {
    val actorSystem = ActorSystem("my-system")
    bind(classOf[ActorSystem]).toInstance(actorSystem)

    // Pekko Materializer
    val materializer = Materializer(actorSystem)
    bind(classOf[Materializer]).toInstance(materializer)

    // ExecutionContext
    bind(classOf[ExecutionContext]).toInstance(actorSystem.dispatcher)

    bind(classOf[MoleculerRegistrationService]).to(classOf[MoleculerRegistrationServiceImpl]).asEagerSingleton()
    bind(classOf[MoleculerServiceDiscovery]).to(classOf[MoleculerServiceDiscoveryImpl]).asEagerSingleton()
    bind(classOf[ServiceUrlProvider]).asEagerSingleton()
    bind(classOf[ApiGatewayService]).to(classOf[ApiGatewayServiceImpl]).asEagerSingleton()
    bind(classOf[PrometheusService]).to(classOf[PrometheusServiceImpl]).asEagerSingleton()

    val prometheusRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    bind(classOf[PrometheusMeterRegistry]).toInstance(prometheusRegistry)
    bind(classOf[MeterRegistry]).toInstance(prometheusRegistry) // equivalent to @Primary
  }
}
