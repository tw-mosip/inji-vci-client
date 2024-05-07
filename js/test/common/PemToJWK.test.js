import { pemToJwk } from "../../src/common/pemToJWK"

describe('conversion of pem to JWK', () => {
    it('should convert given pem to jwk when pem header does not include the algorithm', async () => {
        const publicKeyPem = "-----BEGIN PUBLIC KEY-----\n" +
            "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvFZARHPqouo91YeIIeBpLi1l1AHm+j18LwSVZzwLW3e/OHleZumsbnRDv5wo3IzAMyQZbtvd/gLi+2T36wLj5QwIuctOEisySpFkcf2LvR+NIEg3GNkKSw8SrZt6V9e16eOvOFhy6eIA/HSExZW8lK16gwnWQC8Pzarc8is0QKc/P5GBDDc5Z+yXB7YKvJ7R6HDapvq+1Sn6T5K/d5tMvZUR/yFHUvkOBO+nl0U0QwvlDv5+eCOeus4pHuv8n1P+hpxDocRKjKZ8FrScAyuKmlzDR1NU6uoFBwCOp5nLKl3C07vL6J0mDsu7w8/cJWgJJFQtoV+8ukFvskxaIIMNT755Sh6tiZKCqmmJQDITZmwkm0ikuVtSfF+FGeY9NB9Ow1sdErl8mvIv7t0MKIq3hzBVxr9ksoJ0sp5OhJsFqytbBaUdXTgwNWPNwsz4oAvE3pT6sEvfqMom57YokBNUwTdSNylM4Qa1UpEyAq10yU/V0uU/ZOVUZ1PMDIeSxfA6DnAP226Plnm3u2o/+QcbJsLNaSSigwzFIGOLsIis9nQ5ejtPZApNc0lTsaJ9ZNqSelpzyAEm08WPMaY/yLIBjcFaSr1PwYE3oi9pQdsqdC3uE3rEap+aVki543MfnAwmNtKyvv6DJNZWIklIv10HnuEfx4ErZPfwVNv8zJzpMY8CAwEAAQ==\n" +
            "-----END PUBLIC KEY-----"
        const expectedJWK = { "e": "AQAB", "kty": "RSA", "n": "vFZARHPqouo91YeIIeBpLi1l1AHm-j18LwSVZzwLW3e_OHleZumsbnRDv5wo3IzAMyQZbtvd_gLi-2T36wLj5QwIuctOEisySpFkcf2LvR-NIEg3GNkKSw8SrZt6V9e16eOvOFhy6eIA_HSExZW8lK16gwnWQC8Pzarc8is0QKc_P5GBDDc5Z-yXB7YKvJ7R6HDapvq-1Sn6T5K_d5tMvZUR_yFHUvkOBO-nl0U0QwvlDv5-eCOeus4pHuv8n1P-hpxDocRKjKZ8FrScAyuKmlzDR1NU6uoFBwCOp5nLKl3C07vL6J0mDsu7w8_cJWgJJFQtoV-8ukFvskxaIIMNT755Sh6tiZKCqmmJQDITZmwkm0ikuVtSfF-FGeY9NB9Ow1sdErl8mvIv7t0MKIq3hzBVxr9ksoJ0sp5OhJsFqytbBaUdXTgwNWPNwsz4oAvE3pT6sEvfqMom57YokBNUwTdSNylM4Qa1UpEyAq10yU_V0uU_ZOVUZ1PMDIeSxfA6DnAP226Plnm3u2o_-QcbJsLNaSSigwzFIGOLsIis9nQ5ejtPZApNc0lTsaJ9ZNqSelpzyAEm08WPMaY_yLIBjcFaSr1PwYE3oi9pQdsqdC3uE3rEap-aVki543MfnAwmNtKyvv6DJNZWIklIv10HnuEfx4ErZPfwVNv8zJzpMY8" }
        const jwk = await pemToJwk(publicKeyPem);

        expect(jwk).toEqual(expectedJWK)
    });

    it('should convert given pem to jwk when pem header includes the algorithm', async () => {
        const publicKeyPem = "-----BEGIN RSA PUBLIC KEY-----\nMIICCgKCAgEAqdiAnxqd6go84mQdim7n7NuH9anDxpix04dFWJv1hDgdhsU50TqK\nv51deXP8os2Xu0jwdvH3dOnZAxEbThBdfmhYEKmGivkDj3ogB6EHMToJQ0hUCHwV\nKcjb6QF9aZNPfnm5G+OzKwzSCXgfBfWSiI8PEZSa41LO+y2RJw2cY6+/jRKYgJN9\n14eV//QA+gwIe8EiBkm/f8B906kh/kHHWmRMaEtaTqZECPu8t8Ro1mS8gsun0PUM\nA6kBjQ8A4g56iDzYijIGV7dQzUwtlRWPWIpsCEY5kmvY80msq8+Qt5PZLR56Hd97\ngkq3TAcFKCaBGghJ9b4Wamgo2BGx8ta92+U09N7UUp8LuLkhg1tAowrNT5QnitT8\nO/p6A2Jl46FDI8Q+M/K1mBxF6hyuUBDWD4TZ6k71/kxAYG75wSgPZ8R+sz1OLl+e\nAQ/VbXbx1CTFBCNgzKbNarzeHxXqFcedMdP4xeq2ZiBqePQIhXdz3xyxU9DH7qZY\n9TwznsnlhvclVRV/jpoxe+NBlNHm5DmCN/HX9+kjwPMSuoNxIqiyghChUhU+Gj4o\nFLGlKnhh8Ip1MUX6RI1Cr2OkNGq0f4hu2PRceQu6LRLhtNAlUV60tEdhuO2UVrPP\n/COGWxEyNBM00dWjLH7Xp2rpo5WqBFD8i1jaJQRYUfPoOoFf9kRWZiECAwEAAQ==\n-----END RSA PUBLIC KEY-----\n"
        const expectedJWK = { "e": "AQAB", "kty": "RSA", "n": "qdiAnxqd6go84mQdim7n7NuH9anDxpix04dFWJv1hDgdhsU50TqKv51deXP8os2Xu0jwdvH3dOnZAxEbThBdfmhYEKmGivkDj3ogB6EHMToJQ0hUCHwVKcjb6QF9aZNPfnm5G-OzKwzSCXgfBfWSiI8PEZSa41LO-y2RJw2cY6-_jRKYgJN914eV__QA-gwIe8EiBkm_f8B906kh_kHHWmRMaEtaTqZECPu8t8Ro1mS8gsun0PUMA6kBjQ8A4g56iDzYijIGV7dQzUwtlRWPWIpsCEY5kmvY80msq8-Qt5PZLR56Hd97gkq3TAcFKCaBGghJ9b4Wamgo2BGx8ta92-U09N7UUp8LuLkhg1tAowrNT5QnitT8O_p6A2Jl46FDI8Q-M_K1mBxF6hyuUBDWD4TZ6k71_kxAYG75wSgPZ8R-sz1OLl-eAQ_VbXbx1CTFBCNgzKbNarzeHxXqFcedMdP4xeq2ZiBqePQIhXdz3xyxU9DH7qZY9TwznsnlhvclVRV_jpoxe-NBlNHm5DmCN_HX9-kjwPMSuoNxIqiyghChUhU-Gj4oFLGlKnhh8Ip1MUX6RI1Cr2OkNGq0f4hu2PRceQu6LRLhtNAlUV60tEdhuO2UVrPP_COGWxEyNBM00dWjLH7Xp2rpo5WqBFD8i1jaJQRYUfPoOoFf9kRWZiE" }
        const jwk = await pemToJwk(publicKeyPem);

        expect(jwk).toEqual(expectedJWK)
    });
});
