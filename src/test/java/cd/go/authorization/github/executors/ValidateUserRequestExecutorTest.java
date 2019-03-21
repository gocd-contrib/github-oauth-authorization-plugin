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

package cd.go.authorization.github.executors;

import cd.go.authorization.github.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.requests.ValidateUserRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidateUserRequestExecutorTest {

    private ValidateUserRequestExecutor executor;
    private ValidateUserRequest request;
    private GitHubClientBuilder clientBuilder;

    @Before
    public void setUp() {
        request = mock(ValidateUserRequest.class);
        clientBuilder = mock(GitHubClientBuilder.class);

        AuthConfig authConfig = mock(AuthConfig.class);
        when(request.getAuthConfig()).thenReturn(authConfig);
        when(authConfig.gitHubConfiguration()).thenReturn(mock(GitHubConfiguration.class));

        executor = new ValidateUserRequestExecutor(request, clientBuilder);
    }

    @Test
    public void shouldReturnSuccessResponseWhenUserIsAValidUser() throws Exception {
        GitHub gitHub = mock(GitHub.class);
        when(clientBuilder.from(request.getAuthConfig().gitHubConfiguration()))
                .thenReturn(gitHub);
        when(request.getUsername()).thenReturn("bob");
        when(gitHub.getUser("bob")).thenReturn(mock(GHUser.class));

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("", response.responseBody(), true);
    }

    @Test
    public void shouldReturnErrorResponseWhenUserIsNotAValidUser() throws Exception {
        GitHub gitHub = mock(GitHub.class);
        when(clientBuilder.from(request.getAuthConfig().gitHubConfiguration()))
                .thenReturn(gitHub);
        when(request.getUsername()).thenReturn("bob");
        when(gitHub.getUser("bob")).thenReturn(null);

        GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(500));
    }
}