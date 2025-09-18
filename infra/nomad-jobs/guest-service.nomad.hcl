job "guest-service" {
  datacenters = ["dc1"]
  type        = "service"

  group "guest" {
    count = 1

    network {
      mode = "host"
      port "http" {
        static = 8083
      }
    }

    task "guest" {
      driver = "docker"

      config {
        image        = "ghcr.io/radovan85/hotel-scl/guest-service:main"
        ports        = ["http"]
        network_mode = "host"
      }

      env {
        DB_URL      = "jdbc:mariadb://localhost:3307/hotel-db"
        DB_USERNAME = "root"
        DB_PASSWORD = "1111"
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

      resources {
        cpu    = 500
        memory = 512
      }

      service {
        name     = "guest-service"
        port     = "http"
        tags     = ["guest", "scalatra", "metrics"]
        provider = "nomad"

        check {
          name     = "guest-health"
          type     = "http"
          path     = "/api/health"
          interval = "10s"
          timeout  = "2s"
        }
      }
    }
  }
}
