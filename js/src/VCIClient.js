import { InvalidAccessTokenError } from "./error/InvalidAccessTokenError";
import jwtDecode from "jwt-decode";
import { Logger } from "./common/Logger";
import pem2jwk from 'simple-pem2jwk';
import { encodeB64, replaceCharactersInB64 } from "./common/Util";
import { IssuerMeta } from "./dto/IssuerMeta";

/**
 * 
 * @param {IssuerMeta} issuerMeta 
 * @param {Function} signer 
 * @param {string} accessToken 
 * @param {string} publicKeyPem 
 */
async function requestCredential(issuerMeta, signer, accessToken, publicKeyPem) {
    console.log("1st signer ", signer)

    const header = await buildHeader(publicKeyPem);
    const payload = buildPayload(accessToken, issuerMeta);
    const proofJWT = await generateProof(header, payload, signer);

    const requestBody = {
        format: issuerMeta.credentialFormat,
        credential_definition: {
            '@context': ['https://www.w3.org/2018/credentials/v1'],
            type: issuerMeta.credentialType,
        },
        proof: {
            proof_type: 'jwt',
            jwt: proofJWT,
        },
    };
    const headers = {
        "Content-Type": "application/json",
        "Authorization": `Bearer ` + accessToken
    };
    console.log("headers ",headers)
    console.log("requestBody ",requestBody)
    //fetch request
    const response = await fetch(issuerMeta.credentialEndpoint, {
        method: "POST",
        headers: headers,
        body: JSON.stringify(requestBody)
    })

    console.log("lib proofJWT ", proofJWT)
    console.log("response ", response)

    return response;
}

async function generateProof(header, payload, signer) {
    const header64 = encodeB64(JSON.stringify(header));
    console.log("header64 ", header64)
    const payLoad64 = encodeB64(JSON.stringify(payload));
    console.log("payLoad64 ", payLoad64)
    console.log("signer ", signer)
    const preHash = header64 + '.' + payLoad64;
    const sign = await signer(preHash);
    console.log("sign value ", sign)
    const signature64 = replaceCharactersInB64(sign);
    console.log("signature64 value ", signature64)
    return preHash + '.' + signature64;
}

function buildPayload(accessToken, issuerMeta) {
    try {
        console.log("accessToken ", accessToken)
        console.log("jwtDecode ", jwtDecode)
        const decodedToken = jwtDecode(accessToken);
        console.log("decoded ", decodedToken)
        const payload = {
            iss: decodedToken["client_id"],
            nonce: decodedToken["c_nonce"],
            aud: issuerMeta["credentialAudience"],
            iat: Math.floor(new Date().getTime() / 1000),
            exp: Math.floor(new Date().getTime() / 1000) + 18000,
        };
        console.log("payload ", payload)

        return payload;
    } catch (error) {
        Logger.error(`Error while parsing access token - ${error}`)
        throw new InvalidAccessTokenError(error.message)
    }
}

async function buildHeader(publicKeyPem) {
    console.log("new changes part🎉🎉")
    if (publicKeyPem.includes("-----BEGIN PUBLIC KEY-----")) {
        publicKeyPem = "-----BEGIN RSA PUBLIC KEY-----\n" + publicKeyPem.substring(59).slice(0, -24) + "\n-----END RSA PUBLIC KEY-----"
    }

    const publicKeyJWK = await pem2jwk(publicKeyPem)


    const header = {
        alg: 'RS256', jwk: {
            ...publicKeyJWK, alg: 'RS256', use: 'sig',
        }, typ: 'openid4vci-proof+jwt',
    };
    return header
}

export { requestCredential }
