package com.radovan.scalatra.modules

import com.google.inject.AbstractModule
import com.radovan.scalatra.brokers.{GuestNatsListener, GuestNatsSender}
import com.radovan.scalatra.converter.TempConverter
import com.radovan.scalatra.repositories.GuestRepository
import com.radovan.scalatra.repositories.impl.GuestRepositoryImpl
import com.radovan.scalatra.services.{GuestService, MoleculerRegistrationService, MoleculerServiceDiscovery}
import com.radovan.scalatra.services.impl.{GuestServiceImpl, MoleculerRegistrationServiceImpl, MoleculerServiceDiscoveryImpl}
import com.radovan.scalatra.utils.{JwtUtil, NatsUtils, PublicKeyCache, ServiceUrlProvider}
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
    bind(classOf[GuestService]).to(classOf[GuestServiceImpl]).asEagerSingleton()
    bind(classOf[GuestRepository]).to(classOf[GuestRepositoryImpl]).asEagerSingleton()
    bind(classOf[ServiceUrlProvider]).asEagerSingleton()
    bind(classOf[TempConverter]).asEagerSingleton()
    bind(classOf[JwtUtil]).asEagerSingleton()
    bind(classOf[NatsUtils]).asEagerSingleton()
    bind(classOf[PublicKeyCache]).asEagerSingleton()
    bind(classOf[GuestNatsSender]).asEagerSingleton()
    bind(classOf[GuestNatsListener]).asEagerSingleton()

  }
}