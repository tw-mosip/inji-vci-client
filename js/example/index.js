const express = require("express");
const path = require("path");
const app = express();

const cors = require("cors");
const { createProxyMiddleware } = require("http-proxy-middleware");

const corsOptions = {
  origin: "http://localhost:3000",
  methods: "POST",
  credentials: true,
  optionsSuccessStatus: 200,
};
app.use(cors(corsOptions));

app.use(
  "/api",
  createProxyMiddleware({
    target: "https://esignet.qa-inji.mosip.net",
    changeOrigin: true,
    pathRewrite: {
      "^/api": "/v1/esignet/vci/credential",
    },
  })
);

app.use(express.json());
app.use(express.static(path.join(__dirname, "public")));

const port = 3000;

app.listen(port, async () => {
  console.log(`Server is listening on port ${port}...`);

  const open = await import("open");
  open.default(`http://localhost:${port}`);
});
