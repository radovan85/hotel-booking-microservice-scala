job "reservation-service" {
  datacenters = ["dc1"]
  type        = "service"

  group "reservation" {
    count = 1

    network {
      mode = "host"
      port "http" {
        static = 9002
      }
    }

    task "reservation" {
      driver = "docker"

      config {
        image        = "ghcr.io/radovan85/hotel-scl/reservation-service:main"
        ports        = ["http"]
        network_mode = "host"
      }

      env {
        DB_URL      = "jdbc:mariadb://localhost:3307/hotel-db"
        DB_USERNAME = "root"
        DB_PASSWORD = "1111"
        PLAY_PORT = "9002"
        PLAY_SECRET = "^VtSg88xeAxz<O5c01/?A>^>kd>1<6rgCX@;DCjJcPiTrEN6<IcJv11RxCs8hp<y"
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
        cpu    = 700
        memory = 768
      }

      service {
        name     = "reservation-service"
        port     = "http"
        tags     = ["reservation", "play", "metrics"]
        provider = "nomad"

        check {
          name     = "reservation-health"
          type     = "http"
          path     = "/api/health"
          interval = "10s"
          timeout  = "2s"
        }
      }
    }
  }
}
