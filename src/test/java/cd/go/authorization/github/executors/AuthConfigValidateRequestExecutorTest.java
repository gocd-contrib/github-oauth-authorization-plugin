/*
 * Copyright 2017 ThoughtWorks, Inc.
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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.AuthConfigValidateRequest;
import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuthConfigValidateRequestExecutorTest {

    private GoPluginApiRequest request;

    @Before
    public void setup() {
        request = mock(GoPluginApiRequest.class);
    }

    @Test
    public void shouldValidateMandatoryKeys() throws Exception {
        when(request.requestBody()).thenReturn(new Gson().toJson(singletonMap("AllowedOrganizations", "Foo")));

        GoPluginApiResponse response = AuthConfigValidateRequest.from(request).execute();
        String json = response.responseBody();

        String expectedJSON = "[\n" +
                "  {\n" +
                "    \"key\": \"ClientId\",\n" +
                "    \"message\": \"ClientId must not be blank.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"ClientSecret\",\n" +
                "    \"message\": \"ClientSecret must not be blank.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"PersonalAccessToken\",\n" +
                "    \"message\": \"PersonalAccessToken must not be blank.\"\n" +
                "  }\n" +
                "]";

        JSONAssert.assertEquals(expectedJSON, json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateGitHubEnterpriseUrl() throws Exception {
        when(request.requestBody()).thenReturn("{\n" +
                "  \"ClientId\": \"client-id\",\n" +
                "  \"AllowedOrganizations\": \"example-1,example-2\",\n" +
                "  \"AuthenticateWith\": \"GitHubEnterprise\",\n" +
                "  \"ClientSecret\": \"client-secret\",\n" +
                "  \"PersonalAccessToken\": \"Foobar\"\n" +
                "}");

        GoPluginApiResponse response = AuthConfigValidateRequest.from(request).execute();

        String expectedJSON = "[\n" +
                "  {\n" +
                "    \"key\": \"GitHubEnterpriseUrl\",\n" +
                "    \"message\": \"GitHubEnterpriseUrl must not be blank.\"\n" +
                "  }\n" +
                "]";

        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidatePersonalAccessToken() throws Exception {
        final GitHubConfiguration gitHubConfiguration = GitHubConfiguration.fromJSON("{\n" +
                "  \"ClientId\": \"client-id\",\n" +
                "  \"AllowedOrganizations\": \"example-1,example-2\",\n" +
                "  \"AuthenticateWith\": \"GitHubEnterprise\",\n" +
                "  \"GitHubEnterpriseUrl\": \"https://enterprise.url\",\n" +
                "  \"ClientSecret\": \"client-secret\"" +
                "}");

        when(request.requestBody()).thenReturn(gitHubConfiguration.toJSON());

        GoPluginApiResponse response = AuthConfigValidateRequest.from(request).execute();

        String expectedJSON = "[\n" +
                "  {\n" +
                "    \"key\": \"PersonalAccessToken\",\n" +
                "    \"message\": \"PersonalAccessToken must not be blank.\"\n" +
                "  }\n" +
                "]";

        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }
}