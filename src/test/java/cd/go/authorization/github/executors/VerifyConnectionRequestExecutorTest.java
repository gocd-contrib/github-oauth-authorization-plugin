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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.client.GitHubClientBuilder;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.VerifyConnectionRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VerifyConnectionRequestExecutorTest {
    private VerifyConnectionRequest request;
    private GitHubClientBuilder providerManager;
    private VerifyConnectionRequestExecutor executor;

    @BeforeEach
    public void setup() {
        request = mock(VerifyConnectionRequest.class);
        providerManager = mock(GitHubClientBuilder.class);

        executor = new VerifyConnectionRequestExecutor(request, providerManager);
    }

    @Test
    public void shouldReturnValidationFailedStatusForInvalidAuthConfig() throws Exception {
        when(request.githubConfiguration()).thenReturn(new GitHubConfiguration());

        GoPluginApiResponse response = executor.execute();

        String expectedJSON = """
                {
                  "errors": [
                    {
                      "key": "PersonalAccessToken",
                      "message": "PersonalAccessToken must not be blank."
                    },
                    {
                      "key": "ClientId",
                      "message": "ClientId must not be blank."
                    },
                    {
                      "key": "ClientSecret",
                      "message": "ClientSecret must not be blank."
                    }
                  ],
                  "message": "Validation failed for the given Auth Config",
                  "status": "validation-failed"
                }""";

        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldReturnSuccessResponseOnSuccessfulVerification() throws Exception {
        final GitHubConfiguration gitHubConfiguration = GitHubConfiguration.fromJSON("{ \"ClientId\": \"id\", \"ClientSecret\": \"secret\", \"PersonalAccessToken\": \"pat\"}");

        when(request.githubConfiguration()).thenReturn(gitHubConfiguration);

        GoPluginApiResponse response = executor.execute();

        String expectedJSON = """
                {
                  "message": "Connection ok",
                  "status": "success"
                }""";

        assertThat(response.responseCode()).isEqualTo(200);
        JSONAssert.assertEquals(expectedJSON, response.responseBody(), JSONCompareMode.NON_EXTENSIBLE);
    }
}
