const fetch = require("node-fetch");
const fs = require("fs");
const path = require("path");

module.exports = {
  name: "registry",

  actions: {
    register(ctx) {
      const { name, version, endpoint, metadata } = ctx.params;

      this.registry.set(name, {
        name,
        version,
        endpoint,
        metadata,
        registeredAt: Date.now(),
        lastCheckedAt: null,
        failureCount: 0
      });

      return { status: "registered", name };
    },

    list() {
      return Array.from(this.registry.values());
    },

    async checkHealth() {
      const now = Date.now();
      const removed = [];

      for (const [name, service] of this.registry.entries()) {
        const healthUrl = service.metadata?.healthCheck;
        if (!healthUrl) continue;

        try {
          const res = await fetch(healthUrl, { method: "GET", timeout: 3000 });
          if (!res.ok) throw new Error("Unhealthy");

          service.lastCheckedAt = now;
          service.failureCount = 0;
        } catch (err) {
          service.failureCount = (service.failureCount || 0) + 1;
          service.lastCheckedAt = now;

          if (service.failureCount >= 3) {
            this.registry.delete(name);
            removed.push(name);

            const logEntry = {
              name,
              endpoint: service.endpoint,
              metadata: service.metadata,
              removedAt: now,
              reason: err.message,
              failureCount: service.failureCount
            };

            this.logger.warn(`❌ Removed unhealthy service: ${name}`);
            this.logRemoval(logEntry);
          }
        }
      }

      return { removed };
    }
  },

  created() {
    this.registry = new Map();

    // ⏱️ Periodična provera svakih 60 sekundi
    setInterval(() => {
      this.broker.call("registry.checkHealth").catch(err => {
        this.logger.error("💥 Health check failed:", err.message);
      });
    }, 60000);
  },

  methods: {
    logRemoval(entry) {
      const logPath = path.join(__dirname, "..", "logs", "removal-log.json");
      const logLine = JSON.stringify(entry) + "\n";

      fs.mkdir(path.dirname(logPath), { recursive: true }, (err) => {
        if (err) return this.logger.error("📁 Failed to create log directory:", err.message);

        fs.appendFile(logPath, logLine, (err) => {
          if (err) this.logger.error("📝 Failed to write removal log:", err.message);
        });
      });
    }
  }
};
