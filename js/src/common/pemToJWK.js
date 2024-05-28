import pem2jwk from 'simple-pem2jwk';

export async function pemToJwk(publicKeyPem) {
    console.log("new changes part🎉🎉")
    if (publicKeyPem.includes("-----BEGIN PUBLIC KEY-----")) {
        //TODO: Check is there a way to get the algorithm type from pem rather than hardcoding
        publicKeyPem = "-----BEGIN RSA PUBLIC KEY-----\n" + publicKeyPem.substring(59).slice(0, -24) + "\n-----END RSA PUBLIC KEY-----"
    }

    const publicKeyJWK = await pem2jwk(publicKeyPem)
    return publicKeyJWK

}
