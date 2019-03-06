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

import cd.go.authorization.github.executors.SearchUsersRequestExecutor;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SearchUsersRequestTest {
    @Test
    public void shouldParseRequest() {
        GoPluginApiRequest request = mock(GoPluginApiRequest.class);
        when(request.requestBody()).thenReturn("{\n" +
                "  \"search_term\": \"tom\",\n" +
                "  \"auth_configs\": [\n" +
                "    {\n" +
                "      \"id\": \"github\",\n" +
                "      \"configuration\": {\n" +
                "        \"PersonalAccessToken\": \"personalAccessToken\",\n" +
                "        \"AuthenticateWith\": \"GitHub\",\n" +
                "        \"ClientId\": \"clientId\"," +
                "        \"ClientSecret\": \"clientSecret\"" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}");

        SearchUsersRequest searchUsersRequest = SearchUsersRequest.from(request);

        assertThat(searchUsersRequest.getSearchTerm(), is("tom"));
        assertThat(searchUsersRequest.getAuthConfigs(), hasSize(1));
    }

    @Test
    public void shouldReturnSearchUsersRequestExecutor() {
        GoPluginApiRequest apiRequest = mock(GoPluginApiRequest.class);
        when(apiRequest.requestBody()).thenReturn("{}");

        Request request = SearchUsersRequest.from(apiRequest);

        assertThat(request.executor() instanceof SearchUsersRequestExecutor, is(true));
    }
}