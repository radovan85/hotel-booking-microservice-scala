package com.radovan.scalatra.config

import com.google.inject.{AbstractModule, Guice, Injector}
import com.radovan.scalatra.controllers.{ApiGatewayController, HealthController}
import com.radovan.scalatra.services.impl.{ApiGatewayServiceImpl, MoleculerRegistrationServiceImpl, MoleculerServiceDiscoveryImpl}
import com.radovan.scalatra.services.{ApiGatewayService, MoleculerRegistrationService, MoleculerServiceDiscovery}
import com.radovan.scalatra.utils.ServiceUrlProvider
import org.eclipse.jetty.ee10.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.server.Server
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer

import scala.concurrent.ExecutionContext

object JettyLauncher {
  def main(args: Array[String]): Unit = {
    val injector: Injector = Guice.createInjector(new AbstractModule {
      override def configure(): Unit = {
        // Bind ActorSystem kao singleton
        val actorSystem = ActorSystem("my-system")
        bind(classOf[ActorSystem]).toInstance(actorSystem)

        // Bind Materializer iz ActorSystem
        val materializer = Materializer(actorSystem)
        bind(classOf[Materializer]).toInstance(materializer)

        // Bind ExecutionContext iz ActorSystem dispatcher
        bind(classOf[ExecutionContext]).toInstance(actorSystem.dispatcher)

        // Ostali bindovi
        bind(classOf[MoleculerRegistrationService]).to(classOf[MoleculerRegistrationServiceImpl]).asEagerSingleton()
        bind(classOf[ApiGatewayService]).to(classOf[ApiGatewayServiceImpl]).asEagerSingleton()
        bind(classOf[MoleculerServiceDiscovery]).to(classOf[MoleculerServiceDiscoveryImpl]).asEagerSingleton()
        bind(classOf[ServiceUrlProvider]).asEagerSingleton()
      }
    })

    val server = new Server(8090)
    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    server.setHandler(context)

    try {
      server.start()

      val apiGatewayService = injector.getInstance(classOf[ApiGatewayService])
      val moleculerRegistrationService = injector.getInstance(classOf[MoleculerRegistrationService])
      moleculerRegistrationService.registerService()

      val apiGatewayController = new ApiGatewayController(apiGatewayService)
      val healthController = new HealthController()

      context.addServlet(new ServletHolder("apiGatewayController", apiGatewayController), "/api/*")
      context.addServlet(new ServletHolder("healthController", healthController), "/api/health/*")

      println("✅ Server started at http://localhost:8090")
      println("✅ Health check: http://localhost:8090/api/health")

      server.join()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        System.exit(1)
    }
  }
}
