/*
 * Copyright 2025 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.authorization.github.client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class ProofKey {
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String codeVerifierEncoded;
    private final String codeChallengeEncoded;

    public ProofKey() {
        this.codeVerifierEncoded = generateCodeVerifier();
        this.codeChallengeEncoded = generateCodeChallenge(codeVerifierEncoded);
    }

    private static String generateCodeVerifier() {
        byte[] codeVerifier = new byte[32];
        RANDOM.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    private static String generateCodeChallenge(String codeVerifier) {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        messageDigest.update(bytes, 0, bytes.length);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(messageDigest.digest());
    }

    public String codeVerifierEncoded() {
        return codeVerifierEncoded;
    }

    public String codeChallengeEncoded() {
        return codeChallengeEncoded;
    }
}