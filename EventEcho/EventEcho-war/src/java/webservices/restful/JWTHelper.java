/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package webservices.restful;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.Key;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author elizabeth
 */
public class JWTHelper {
    //specify your own random secret String

    private static String SECRET = "asdioasg18923t120rbgflodigy10123489126421gjlkasgdlsa";

    public static Key hmacKey = new SecretKeySpec(Base64.getDecoder().decode(SECRET),
            SignatureAlgorithm.HS256.getJcaName());
}
