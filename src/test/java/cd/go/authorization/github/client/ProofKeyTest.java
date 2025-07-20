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

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProofKeyTest {

    @Test
    public void shouldGenerate() {
        ProofKey proofKey = new ProofKey();

        assertThat(proofKey).satisfies(args -> {
            assertThat(args.codeVerifierEncoded()).matches("[A-Za-z0-9_-]{43}");
            assertThat(args.codeChallengeEncoded()).matches("[A-Za-z0-9_-]{43}");
            assertThat(args.codeChallengeEncoded()).isNotEqualTo(args.codeVerifierEncoded());
        });
    }
}