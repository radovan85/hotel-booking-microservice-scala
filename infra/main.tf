terraform {
  required_providers {
    docker = {
      source  = "kreuzwerker/docker"
      version = "~> 2.25.0"
    }
    nomad = {
      source  = "hashicorp/nomad"
      version = "~> 1.4.0"
    }
  }
}

provider "docker" {
  host = "unix:///var/run/docker.sock"
}

provider "nomad" {
  address = "http://127.0.0.1:4646"
}

# === Consul image i kontejner ===
resource "docker_image" "consul" {
  name = var.consul_image
}

resource "docker_container" "consul" {
  name         = var.consul_container_name
  image        = docker_image.consul.latest
  network_mode = "host"

  command = [
    "agent", "-dev", "-client=0.0.0.0"
  ]

  restart = "unless-stopped"
}

# === Registry service job ===
resource "nomad_job" "registry_service" {
  jobspec = file("${path.module}/nomad-jobs/registry-service.nomad.hcl")
}

resource "null_resource" "registry_ready" {
  depends_on = [nomad_job.registry_service]

  provisioner "local-exec" {
    command = "bash -c 'while ! curl -sf http://127.0.0.1:3400/health; do sleep 10; done'"
  }
}


# === API Gateway job ===
resource "nomad_job" "api_gateway" {
  jobspec    = file("${path.module}/nomad-jobs/api-gateway.nomad.hcl")
  depends_on = [null_resource.registry_ready]
}

# === NATS job ===
resource "nomad_job" "nats_server" {
  jobspec = file("${path.module}/nomad-jobs/nats.nomad.hcl")
}

# === MariaDB job ===
resource "nomad_job" "mariadb" {
  jobspec = file("${path.module}/nomad-jobs/mariadb.nomad.hcl")
}


resource "null_resource" "api_gateway_ready" {
  depends_on = [nomad_job.api_gateway]

  provisioner "local-exec" {
    command = "bash -c 'while ! curl -sf http://127.0.0.1:8090/api/health; do sleep 10; done'"
  }
}

resource "null_resource" "nats_ready" {
  depends_on = [nomad_job.nats_server]

  provisioner "local-exec" {
    command = "bash -c 'while ! curl -sf --max-time 5 http://127.0.0.1:8222/; do sleep 10; done'"
  }
}

resource "null_resource" "mariadb_ready" {
  depends_on = [nomad_job.mariadb]

  provisioner "local-exec" {
    command = "bash -c 'while ! nc -z 127.0.0.1 3307; do sleep 10; done'"
  }
}

# === Prometheus job ===
resource "nomad_job" "prometheus" {
  jobspec    = file("${path.module}/nomad-jobs/prometheus.nomad.hcl")
  depends_on = [null_resource.api_gateway_ready]
}

# === Grafana job ===
resource "nomad_job" "grafana" {
  jobspec    = file("${path.module}/nomad-jobs/grafana.nomad.hcl")
  depends_on = [null_resource.api_gateway_ready]
}

# === Auth service job ===
resource "nomad_job" "auth_service" {
  jobspec    = file("${path.module}/nomad-jobs/auth-service.nomad.hcl")
  depends_on = [
    null_resource.registry_ready,
    null_resource.mariadb_ready,
    null_resource.nats_ready
  ]
}


resource "null_resource" "auth_service_ready" {
  depends_on = [nomad_job.auth_service]

  provisioner "local-exec" {
    command = "bash -c 'while ! curl -sf http://127.0.0.1:8081/api/health; do sleep 10; done'"
  }
}


# === Guest service job ===
resource "nomad_job" "guest_service" {
  jobspec    = file("${path.module}/nomad-jobs/guest-service.nomad.hcl")
  depends_on = [
    null_resource.registry_ready,
    null_resource.mariadb_ready,
    null_resource.nats_ready,
    null_resource.auth_service_ready
  ]
}

# === Room service job ===
resource "nomad_job" "room_service" {
  jobspec    = file("${path.module}/nomad-jobs/room-service.nomad.hcl")
  depends_on = [
    null_resource.registry_ready,
    null_resource.mariadb_ready,
    null_resource.nats_ready,
    null_resource.auth_service_ready
  ]
}

# === Reservation service job ===
resource "nomad_job" "reservation_service" {
  jobspec    = file("${path.module}/nomad-jobs/reservation-service.nomad.hcl")
  depends_on = [
    null_resource.registry_ready,
    null_resource.mariadb_ready,
    null_resource.nats_ready,
    null_resource.auth_service_ready
  ]
}

# === Vue frontend job ===
resource "nomad_job" "vue_frontend" {
  jobspec    = file("${path.module}/nomad-jobs/vue-frontend.hcl")
  depends_on = [
    null_resource.api_gateway_ready,
    null_resource.auth_service_ready
  ]
}