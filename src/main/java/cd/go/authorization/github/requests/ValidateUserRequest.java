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

import cd.go.authorization.github.executors.RequestExecutor;
import cd.go.authorization.github.executors.ValidateUserRequestExecutor;
import cd.go.authorization.github.models.AuthConfig;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;

public class ValidateUserRequest extends Request {
    @Expose
    @SerializedName("auth_config")
    private AuthConfig authConfig;

    @Expose
    @SerializedName("username")
    private String username;

    public static Request from(GoPluginApiRequest request) {
        return Request.from(request, ValidateUserRequest.class);
    }

    @Override
    public RequestExecutor executor() {
        return new ValidateUserRequestExecutor(this);
    }

    public AuthConfig getAuthConfig() {
        return authConfig;
    }

    public String getUsername() {
        return username;
    }
}
