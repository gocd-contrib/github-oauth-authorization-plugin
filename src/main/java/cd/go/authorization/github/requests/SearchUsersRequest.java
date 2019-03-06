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
import cd.go.authorization.github.models.AuthConfig;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

import java.util.List;

public class SearchUsersRequest extends Request {
    @Expose
    @SerializedName("auth_configs")
    private List<AuthConfig> authConfigs;

    @Expose
    @SerializedName("search_term")
    private String searchTerm;

    public static SearchUsersRequest from(GoPluginApiRequest request) {
        return Request.from(request, SearchUsersRequest.class);
    }

    @Override
    public SearchUsersRequestExecutor executor() {
        return new SearchUsersRequestExecutor(this);
    }

    public List<AuthConfig> getAuthConfigs() {
        return authConfigs;
    }

    public String getSearchTerm() {
        return searchTerm;
    }
}
