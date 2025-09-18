job "room-service" {
  datacenters = ["dc1"]
  type        = "service"

  group "room" {
    count = 1

    network {
      mode = "host"
      port "http" {
        static = 9001
      }
    }

    task "room" {
      driver = "docker"

      config {
        image        = "ghcr.io/radovan85/hotel-scl/room-service:main"
        ports        = ["http"]
        network_mode = "host"
      }

      env {
        DB_URL      = "jdbc:mariadb://localhost:3307/hotel-db"
        DB_USERNAME = "root"
        DB_PASSWORD = "1111"
        PLAY_PORT = "9001"
        PLAY_SECRET = "M^;pDYFx;/:YR4z<@t<=eI8a<Pk<FDeciChApqC:5PVk8g_c_v5ppe7?:Wb_tBNE"
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
        name     = "room-service"
        port     = "http"
        tags     = ["room", "play", "metrics"]
        provider = "nomad"

        check {
          name     = "room-health"
          type     = "http"
          path     = "/api/health"
          interval = "10s"
          timeout  = "2s"
        }
      }
    }
  }
}
