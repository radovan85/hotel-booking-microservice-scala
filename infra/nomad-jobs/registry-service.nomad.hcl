job "registry-service" {
  datacenters = ["dc1"]
  type        = "service"

  group "registry-service" {
    count = 1

    # 🛜 Host networking: izbegavamo CNI/iptables komplikacije u WSL-u
    network {
      mode = "host"
      port "http" {
        static = 3400  # Moleculer registry API port
      }
    }

    update {
      stagger      = "30s"
      max_parallel = 1
    }

    task "registry-service" {
      driver = "docker"

      config {
        image        = "ghcr.io/radovan85/hotel-scl/registry-service:main"
        ports        = ["http"]
        network_mode = "host"  # Mora da se poklapa sa group.network.mode
      }

      env {
        NODE_ENV  = "production"
        LOG_LEVEL = "info"  # Za bolju observabilnost
      }

      resources {
        cpu    = 500
        memory = 512
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

      service {
        name     = "registry-service"
        port     = "http"
        tags     = ["moleculer", "registry", "discovery"]
        provider = "nomad"

        check {
          name     = "registry-service-health"
          type     = "http"
          path     = "/health"
          interval = "10s"
          timeout  = "2s"
          method   = "GET"
        }
      }
    }
  }
}
