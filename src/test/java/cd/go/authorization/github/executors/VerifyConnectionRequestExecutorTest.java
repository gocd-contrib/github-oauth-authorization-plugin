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
import cd.go.authorization.github.GitHubClientBuilder;
import cd.go.authorization.github.requests.VerifyConnectionRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyConnectionRequestExecutorTest {
    private VerifyConnectionRequest request;
    private GitHubClientBuilder providerManager;
    private VerifyConnectionRequestExecutor executor;

    @Before
    public void setup() {
        request = mock(VerifyConnectionRequest.class);
        providerManager = mock(GitHubClientBuilder.class);

        executor = new VerifyConnectionRequestExecutor(request, providerManager);
    }

    @Test
    public void shouldReturnValidationFailedStatusForInvalidAuthConfig() throws Exception {
        when(request.githubConfiguration()).thenReturn(new GitHubConfiguration());

        GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"errors\": [\n" +
                "    {\n" +
                "      \"key\": \"PersonalAccessToken\",\n" +
                "      \"message\": \"PersonalAccessToken must not be blank.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"key\": \"ClientId\",\n" +
                "      \"message\": \"ClientId must not be blank.\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"key\": \"ClientSecret\",\n" +
                "      \"message\": \"ClientSecret must not be blank.\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"message\": \"Validation failed for the given Auth Config\",\n" +
                "  \"status\": \"validation-failed\"\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldReturnSuccessResponseOnSuccessfulVerification() throws Exception {
        final GitHubConfiguration gitHubConfiguration = mock(GitHubConfiguration.class);

        when(request.githubConfiguration()).thenReturn(gitHubConfiguration);

        GoPluginApiResponse response = executor.execute();

        String expectedJSON = "{\n" +
                "  \"message\": \"Connection ok\",\n" +
                "  \"status\": \"success\"\n" +
                "}";

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }
}