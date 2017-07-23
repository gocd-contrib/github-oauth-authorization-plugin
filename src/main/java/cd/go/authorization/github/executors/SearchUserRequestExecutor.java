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
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;

import java.util.ArrayList;
import java.util.List;

import static cd.go.authorization.github.utils.Util.GSON;

public class SearchUserRequestExecutor implements RequestExecutor {
    private final SearchUserRequest request;
    private final GitHubProviderManager providerManager;
    private final int MAX_SEARCH_RESULT_COUNT = 10;

    public SearchUserRequestExecutor(SearchUserRequest request) {
        this(request, GitHubProviderManager.getInstance());
    }

    SearchUserRequestExecutor(SearchUserRequest request, GitHubProviderManager providerManager) {
        this.request = request;
        this.providerManager = providerManager;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        final List<User> users = new ArrayList<>();
        for (AuthConfig authConfig : request.authConfigs()) {
            final GitHubProvider provider = providerManager.getGitHubProvider(authConfig);
            users.addAll(provider.searchUsers(request.searchTerm(), MAX_SEARCH_RESULT_COUNT - users.size()));

            if (users.size() >= 10) {
                break;
            }
        }

        return DefaultGoPluginApiResponse.success(GSON.toJson(users));
    }
}
