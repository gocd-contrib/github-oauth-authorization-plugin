/*
 * Copyright 2019 ThoughtWorks, Inc.
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

import cd.go.authorization.github.executors.ValidateUserRequestExecutor;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidateUserRequestTest {

    @Test
    public void shouldParseGoAPIRequestToValidateUserRequest() {
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        when(apiRequest.requestBody()).thenReturn("{\n" +
                "  \"auth_config\": {\n" +
                "    \"configuration\": {\n" +
                "      \"AllowedOrganizations\": \"\",\n" +
                "      \"AuthenticateWith\": \"GitHub\",\n" +
                "      \"AuthorizeUsing\": \"PersonalAccessToken\",\n" +
                "      \"ClientId\": \"Foo\",\n" +
                "      \"ClientSecret\": \"bar\",\n" +
                "      \"GitHubEnterpriseUrl\": \"\",\n" +
                "      \"PersonalAccessToken\": \"Baz\"\n" +
                "    },\n" +
                "    \"id\": \"GitHub\"\n" +
                "  },\n" +
                "  \"username\": \"bob\"\n" +
                "}");

        ValidateUserRequest request = (ValidateUserRequest) ValidateUserRequest.from(apiRequest);

        assertThat(request.getUsername(), is("bob"));
        assertThat(request.getAuthConfig().getId(), is("GitHub"));
    }

    @Test
    public void shouldReturnValidateUserExecutor() {
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        when(apiRequest.requestBody()).thenReturn("{\n" +
                "  \"auth_config\": {\n" +
                "    \"configuration\": {\n" +
                "      \"AllowedOrganizations\": \"\",\n" +
                "      \"AuthenticateWith\": \"GitHub\",\n" +
                "      \"AuthorizeUsing\": \"PersonalAccessToken\",\n" +
                "      \"ClientId\": \"Foo\",\n" +
                "      \"ClientSecret\": \"bar\",\n" +
                "      \"GitHubEnterpriseUrl\": \"\",\n" +
                "      \"PersonalAccessToken\": \"Baz\"\n" +
                "    },\n" +
                "    \"id\": \"GitHub\"\n" +
                "  },\n" +
                "  \"username\": \"bob\"\n" +
                "}");

        Request request = ValidateUserRequest.from(apiRequest);

        assertThat(request.executor() instanceof ValidateUserRequestExecutor, is(true));
    }
}