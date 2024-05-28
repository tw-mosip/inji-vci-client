import forge from "node-forge";

export function encodeB64(str) {
    const encodedB64 = forge.util.encode64(str);
    return replaceCharactersInB64(encodedB64);
}

export function replaceCharactersInB64(encodedB64) {
    return encodedB64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}
