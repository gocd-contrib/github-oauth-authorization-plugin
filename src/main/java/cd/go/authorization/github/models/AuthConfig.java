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

package cd.go.authorization.github.models;

import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import static cd.go.authorization.github.utils.Util.GSON;

public class AuthConfig {
    @Expose
    @SerializedName("id")
    private String id;

    @Expose
    @SerializedName("configuration")
    private GitHubConfiguration gitHubConfiguration;

    public String getId() {
        return id;
    }

    public GitHubConfiguration gitHubConfiguration() {
        return gitHubConfiguration;
    }

    public static List<AuthConfig> fromJSONList(String requestBody) {
        JsonObject jsonObject = GSON.fromJson(requestBody, JsonObject.class);
        Type type = new TypeToken<List<AuthConfig>>() {
        }.getType();
        return GSON.fromJson(jsonObject.get("auth_configs").toString(), type);
    }

    public static AuthConfig fromJSON(String json) {
        return GSON.fromJson(json, AuthConfig.class);
    }

}
