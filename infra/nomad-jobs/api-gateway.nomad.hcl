job "api-gateway" {
  datacenters = ["dc1"]
  type        = "service"

  group "gateway" {
    count = 1

    # Koristimo host mrežu da izbegnemo CNI/iptables komplikacije u WSL-u
    network {
      mode = "host"
      port "http" {
        static = 8090
      }
    }

    task "gateway" {
      driver = "docker"

      config {
        image        = "ghcr.io/radovan85/hotel-scl/api-gateway:main"
        ports        = ["http"]
        network_mode = "host"  # Mora da se poklapa sa group.network.mode
      }

      resources {
        cpu    = 500
        memory = 512
      }

      service {
        name     = "api-gateway"
        port     = "http"
        tags     = ["gateway", "edge", "routing"]
        provider = "nomad"

        check {
          name     = "gateway-health"
          type     = "http"
          path     = "/api/health"
          interval = "10s"
          timeout  = "2s"
          method   = "GET"
        }
      }

      env {
        JAVA_OPTS         = "-Xms128m -Xmx256m"
        GATEWAY_HOSTNAME  = "localhost"  # Pošto koristi host mrežu
        REGISTRY_URL = "http://localhost:3400/services"  
      }
      
      lifecycle {
        hook    = "prestart"
        sidecar = false
      }

      restart {
        attempts = 10
        interval = "5m"
        delay    = "15s"
        mode     = "delay"
      }
    }
  }
}
