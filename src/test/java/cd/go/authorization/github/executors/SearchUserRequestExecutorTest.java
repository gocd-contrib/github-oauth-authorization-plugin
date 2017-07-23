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

import cd.go.authorization.github.GitHubProvider;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.User;
import cd.go.authorization.github.providermanager.GitHubProviderManager;
import cd.go.authorization.github.requests.SearchUserRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.List;

import static cd.go.authorization.github.utils.Util.GSON;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchUserRequestExecutorTest {
    @Test
    public void shouldSearchUsers() throws Exception {
        final SearchUserRequest request = mock(SearchUserRequest.class);
        final GitHubProviderManager providerManager = mock(GitHubProviderManager.class);
        final GitHubProvider provider = mock(GitHubProvider.class);
        final AuthConfig authConfig = mock(AuthConfig.class);
        final List<User> users = asList(new User("bob", "B. Ford", "bob@example.com"));

        when(providerManager.getGitHubProvider(authConfig)).thenReturn(provider);
        when(provider.searchUsers(request.searchTerm(), 10)).thenReturn(users);
        when(request.authConfigs()).thenReturn(asList(authConfig));

        final SearchUserRequestExecutor executor = new SearchUserRequestExecutor(request, providerManager);

        final GoPluginApiResponse response = executor.execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals(GSON.toJson(users), response.responseBody(), true);
    }
}