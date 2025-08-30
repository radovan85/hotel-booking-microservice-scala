// api.js
const express = require("express");
const bodyParser = require("body-parser");
const { ServiceBroker } = require("moleculer");
const RegistryService = require("../services/registry.service");


const broker = new ServiceBroker({ nodeID: "registry-node" });
broker.createService(RegistryService);
broker.start();

const app = express();
app.use(bodyParser.json());

app.post("/register", async (req, res) => {
  const result = await broker.call("registry.register", req.body);
  res.json(result);
});

app.get("/services", async (req, res) => {
  const result = await broker.call("registry.list");
  res.json(result);
});

app.listen(3000, () => {
  console.log("Registry server listening on port 3000");
});
