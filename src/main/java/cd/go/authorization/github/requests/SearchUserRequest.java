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

package cd.go.authorization.github.requests;

import cd.go.authorization.github.executors.RequestExecutor;
import cd.go.authorization.github.executors.SearchUserRequestExecutor;
import cd.go.authorization.github.models.AuthConfig;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

import java.util.List;

public class SearchUserRequest extends Request {
    @Expose
    @SerializedName("search_term")
    private String searchTerm;

    @Expose
    @SerializedName("auth_configs")
    private List<AuthConfig> authConfigs;

    @Override
    public RequestExecutor executor() {
        return new SearchUserRequestExecutor(this);
    }

    public String searchTerm() {
        return searchTerm;
    }

    public List<AuthConfig> authConfigs() {
        return authConfigs;
    }

    public static SearchUserRequest from(GoPluginApiRequest apiRequest) {
        return Request.from(apiRequest, SearchUserRequest.class);
    }
}
