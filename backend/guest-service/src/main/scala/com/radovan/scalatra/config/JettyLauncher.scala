package com.radovan.scalatra.config

import com.google.inject.{Guice, Injector}
import com.radovan.scalatra.controllers.{GuestController, HealthController, PrometheusController}
import com.radovan.scalatra.modules.{AutoBindModule, HibernateModule, MapperModule}
import com.radovan.scalatra.services.MoleculerRegistrationService
import org.eclipse.jetty.ee10.servlet.{ServletContextHandler, ServletHolder}
import org.eclipse.jetty.server.Server

object JettyLauncher {

  def main(args: Array[String]): Unit = {
    // Kreiraj Guice injector sa više modula, uključujući HibernateModule
    val injector: Injector = Guice.createInjector(
      new HibernateModule,    // Hibernate konfiguracija
      new MapperModule,       // Mapper konfiguracija
      new AutoBindModule      // Automatsko bindovanje servisa i kontrolera
    )

    // Podešavanje Jetty servera na portu 8083
    val server = new Server(8083)
    val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
    context.setContextPath("/")
    server.setHandler(context)

    try {
      server.start()

      // Registruj servis u Eureki
      val eurekaRegistrationService = injector.getInstance(classOf[MoleculerRegistrationService])
      eurekaRegistrationService.registerService()

      // Dohvati kontrolere iz Guice injector-a
      val guestController = injector.getInstance(classOf[GuestController])
      val healthController = injector.getInstance(classOf[HealthController])
      val prometheusController = injector.getInstance(classOf[PrometheusController])

      // Dodaj kontrolere kao servlet-e na odgovarajuće rute
      context.addServlet(new ServletHolder("guestController", guestController), "/api/guests/*")
      context.addServlet(new ServletHolder("healthController", healthController), "/api/health/*")
      context.addServlet(new ServletHolder("prometheusController", prometheusController), "/prometheus/*")

      println("✅ Server started at http://localhost:8083")
      println("✅ Health check: http://localhost:8083/api/health")

      server.join()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        System.exit(1)
    }
  }
}