import {requestCredential} from "inji-vci-client";

document.addEventListener("DOMContentLoaded", () => {
  document
    .getElementById("downloadButton")
    .addEventListener("click", async function () {
      try {
        const issuerMeta = {
          credentialAudience: "<>",
          credentialEndpoint:
            "<>",
          downloadTimeoutInMilliSeconds: 30000,
          credentialType: ["", ""],
          credentialFormat: "<>",
        };

        const proof ="<>";
        const accessToken ="<>";

        const result = await requestCredential(issuerMeta, proof, accessToken);

        document.getElementById(
          "message"
        ).textContent = `vc download success: ${result}`;
      } catch (error) {
        document.getElementById(
          "message"
        ).textContent = `vc download failed: ${error.message}`;
      }
    });
});
