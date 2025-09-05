package com.radovan.spring.config

import com.radovan.spring.services.MoleculerRegistrationService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MoleculerClientRegistrationInitializer @Autowired()(private val moleculerRegistrationService: MoleculerRegistrationService) {

  @PostConstruct
  def initialize(): Unit = {
    try {
      moleculerRegistrationService.registerService()
    } catch {
      case e: Exception =>
        println(s"Error during service registration: ${e.getMessage}")
        e.printStackTrace()
    }
  }
}