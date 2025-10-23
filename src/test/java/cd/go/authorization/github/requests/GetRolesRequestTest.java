/*
 * Copyright 2022 Thoughtworks, Inc.
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

import cd.go.authorization.github.executors.GetRolesExecutor;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GetRolesRequestTest {
    @Test
    public void shouldParseRequest() {
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        when(apiRequest.requestBody()).thenReturn("""
                {
                  "auth_config": {
                    "configuration": {
                      "AllowedOrganizations": "",
                      "AuthenticateWith": "GitHub",
                      "AuthorizeUsing": "PersonalAccessToken",
                      "ClientId": "Foo",
                      "ClientSecret": "bar",
                      "GitHubEnterpriseUrl": "",
                      "PersonalAccessToken": "Baz"
                    },
                    "id": "GitHub"
                  },
                   "role_configs": [],
                  "username": "bob"
                }""");

        GetRolesRequest request = (GetRolesRequest) GetRolesRequest.from(apiRequest);

        assertThat(request.getUsername()).isEqualTo("bob");
        assertThat(request.getAuthConfig().getId()).isEqualTo("GitHub");
        assertThat(request.getRoles()).hasSize(0);
    }

    @Test
    public void shouldReturnValidExecutor() {
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        when(apiRequest.requestBody()).thenReturn("""
                {
                  "auth_config": {
                    "configuration": {
                      "AllowedOrganizations": "",
                      "AuthenticateWith": "GitHub",
                      "AuthorizeUsing": "PersonalAccessToken",
                      "ClientId": "Foo",
                      "ClientSecret": "bar",
                      "GitHubEnterpriseUrl": "",
                      "PersonalAccessToken": "Baz"
                    },
                    "id": "GitHub"
                  },
                   "role_configs": [],
                  "username": "bob"
                }""");

        GetRolesRequest request = (GetRolesRequest) GetRolesRequest.from(apiRequest);
        assertThat(request.executor() instanceof GetRolesExecutor).isEqualTo(true);
    }
}