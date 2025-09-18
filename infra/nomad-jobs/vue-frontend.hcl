job "vue-frontend" {
  datacenters = ["dc1"]
  type        = "service"

  group "frontend" {
    count = 1

    network {
      mode = "host"

      port "http" {
        static = 3000
      }
    }

    task "vue" {
      driver = "docker"

      config {
        image        = "ghcr.io/radovan85/hotel-scl/vue-frontend:main"
        ports        = ["http"]
        network_mode = "host"
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
        name     = "vue-frontend"
        port     = "http"
        tags     = ["frontend", "vue", "nginx"]
        provider = "nomad"

        check {
          name          = "vue-health"
          type          = "http"
          path          = "/"
          interval      = "10s"
          timeout       = "2s"
          address_mode  = "host"
        }
      }

      
    }
  }
}
