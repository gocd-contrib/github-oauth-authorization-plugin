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

package cd.go.authorization.github.requests;

import cd.go.authorization.github.exceptions.NoAuthorizationConfigurationException;
import cd.go.authorization.github.models.AuthConfig;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthConfigurableTest {
    @Test
    public void shouldErrorOutIfAuthConfigsAreNull() {
        AuthConfigurable authConfigurable = () -> null;
        assertThatThrownBy(authConfigurable::firstAuthConfig)
                .isInstanceOf(NoAuthorizationConfigurationException.class)
                .hasMessageContaining("[AuthConfigurableTest")
                .hasMessageContaining("No authorization configuration found.");
    }

    @Test
    public void shouldErrorOutIfAuthConfigsAreEmpty() {
        AuthConfigurable authConfigurable = Collections::emptyList;
        assertThatThrownBy(authConfigurable::firstAuthConfig)
                .isInstanceOf(NoAuthorizationConfigurationException.class)
                .hasMessageContaining("[AuthConfigurableTest")
                .hasMessageContaining("No authorization configuration found.");
    }

    @Test
    public void shouldReturnFirstAuthConfig() {
        AuthConfig first = new AuthConfig();
        AuthConfigurable authConfigurable = () -> List.of(first, new AuthConfig());
        assertThat(authConfigurable.firstAuthConfig())
                .isSameAs(first);
    }
}