import pem2jwk from 'simple-pem2jwk';

export async function pemToJwk(publicKeyPem) {
    if (publicKeyPem.includes("-----BEGIN PUBLIC KEY-----")) {
        //TODO: Remove hardcoded key type
        publicKeyPem = "-----BEGIN RSA PUBLIC KEY-----\n" + publicKeyPem.substring(59).slice(0, -24) + "\n-----END RSA PUBLIC KEY-----"
    }

    const publicKeyJWK = await pem2jwk(publicKeyPem)
    return publicKeyJWK

}