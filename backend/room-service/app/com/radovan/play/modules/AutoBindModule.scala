package com.radovan.play.modules

import com.google.inject.AbstractModule
import com.radovan.play.brokers.{RoomNatsListener, RoomNatsSender}
import com.radovan.play.converter.TempConverter
import com.radovan.play.repositories.{RoomCategoryRepository, RoomRepository}
import com.radovan.play.repositories.impl.{RoomCategoryRepositoryImpl, RoomRepositoryImpl}
import com.radovan.play.services.{MoleculerRegistrationService, MoleculerServiceDiscovery, RoomCategoryService, RoomService}
import com.radovan.play.services.impl.{MoleculerRegistrationServiceImpl, MoleculerServiceDiscoveryImpl, RoomCategoryServiceImpl, RoomServiceImpl}
import com.radovan.play.utils.{JwtUtil, NatsUtils, PublicKeyCache, ServiceUrlProvider}


class AutoBindModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[RoomService]).to(classOf[RoomServiceImpl]).asEagerSingleton()
    bind(classOf[RoomCategoryService]).to(classOf[RoomCategoryServiceImpl]).asEagerSingleton()
    bind(classOf[MoleculerRegistrationService]).to(classOf[MoleculerRegistrationServiceImpl]).asEagerSingleton()
    bind(classOf[MoleculerServiceDiscovery]).to(classOf[MoleculerServiceDiscoveryImpl]).asEagerSingleton()
    bind(classOf[RoomRepository]).to(classOf[RoomRepositoryImpl]).asEagerSingleton()
    bind(classOf[RoomCategoryRepository]).to(classOf[RoomCategoryRepositoryImpl]).asEagerSingleton()
    bind(classOf[TempConverter]).asEagerSingleton()
    bind(classOf[JwtUtil]).asEagerSingleton()
    bind(classOf[NatsUtils]).asEagerSingleton()
    bind(classOf[PublicKeyCache]).asEagerSingleton()
    bind(classOf[ServiceUrlProvider]).asEagerSingleton()
    bind(classOf[RoomNatsListener]).asEagerSingleton()
    bind(classOf[RoomNatsSender]).asEagerSingleton()


  }
}