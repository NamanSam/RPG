package com.codequest.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Service
public class JwtService {
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long ttl;
    public JwtService(@Value("${app.jwt-secret}") String secret, @Value("${app.jwt-ttl-seconds}") long ttl) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET must be at least 32 bytes.");
        var key = new SecretKeySpec(bytes, "HmacSHA256");
        encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        var jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtDecoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("codequest-rpg"));
        decoder = jwtDecoder;
        this.ttl = ttl;
    }
    public String issue(long userId) {
        Instant now = Instant.now();
        var claims = JwtClaimsSet.builder().issuer("codequest-rpg").subject(Long.toString(userId))
                .issuedAt(now).expiresAt(now.plusSeconds(ttl)).build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
    public long userId(String token) { return Long.parseLong(decoder.decode(token).getSubject()); }
    public long ttl() { return ttl; }
}
