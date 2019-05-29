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

import cd.go.authorization.github.annotation.ProfileField;
import cd.go.authorization.github.annotation.Validatable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cd.go.authorization.github.utils.Util.*;

public class GitHubConfiguration implements Validatable {
    private static final String GITHUB_URL = "https://github.com";
    private static final String GITHUB_ENTERPRISE_API_SUFFIX = "/api/v3/";

    @Expose
    @SerializedName("ClientId")
    @ProfileField(key = "ClientId", required = true, secure = true)
    private String clientId;

    @Expose
    @SerializedName("ClientSecret")
    @ProfileField(key = "ClientSecret", required = true, secure = true)
    private String clientSecret;

    @Expose
    @SerializedName("AuthenticateWith")
    @ProfileField(key = "AuthenticateWith", required = false, secure = false)
    private AuthenticateWith authenticateWith;

    @Expose
    @SerializedName("GitHubEnterpriseUrl")
    @ProfileField(key = "GitHubEnterpriseUrl", required = false, secure = false)
    private String gitHubEnterpriseUrl;

    @Expose
    @SerializedName("AllowedOrganizations")
    @ProfileField(key = "AllowedOrganizations", required = false, secure = false)
    private String allowedOrganizations;

    @Expose
    @SerializedName("PersonalAccessToken")
    @ProfileField(key = "PersonalAccessToken", required = true, secure = true)
    private String personalAccessToken;


    public GitHubConfiguration() {
    }

    public GitHubConfiguration(String clientId, String clientSecret) {
        this(clientId, clientSecret, AuthenticateWith.GITHUB, null, null);
    }

    public GitHubConfiguration(String clientId, String clientSecret, AuthenticateWith authenticateWith, String gitHubEnterpriseUrl, String allowedOrganizations) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.authenticateWith = authenticateWith;
        this.gitHubEnterpriseUrl = gitHubEnterpriseUrl;
        this.allowedOrganizations = allowedOrganizations;
    }

    public String clientId() {
        return clientId;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public String toJSON() {
        return GSON.toJson(this);
    }

    public AuthenticateWith authenticateWith() {
        return authenticateWith;
    }

    public String gitHubEnterpriseUrl() {
        return gitHubEnterpriseUrl;
    }

    public String gitHubEnterpriseApiUrl() {
        return gitHubEnterpriseUrl.concat(GITHUB_ENTERPRISE_API_SUFFIX);
    }

    public String apiUrl() {
        return authenticateWith == AuthenticateWith.GITHUB ? GITHUB_URL : gitHubEnterpriseUrl;
    }

    public String scope() {
        return "user:email";
    }

    public static GitHubConfiguration fromJSON(String json) {
        return GSON.fromJson(json, GitHubConfiguration.class);
    }

    public List<String> organizationsAllowed() {
        return listFromCommaSeparatedString(toLowerCase(allowedOrganizations));
    }

    public Map<String, String> toProperties() {
        return GSON.fromJson(toJSON(), new TypeToken<Map<String, String>>() {
        }.getType());
    }

    public String personalAccessToken() {
        return personalAccessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitHubConfiguration that = (GitHubConfiguration) o;
        return Objects.equals(clientId, that.clientId) &&
                Objects.equals(clientSecret, that.clientSecret) &&
                authenticateWith == that.authenticateWith &&
                Objects.equals(gitHubEnterpriseUrl, that.gitHubEnterpriseUrl) &&
                Objects.equals(allowedOrganizations, that.allowedOrganizations) &&
                Objects.equals(personalAccessToken, that.personalAccessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, clientSecret, authenticateWith, gitHubEnterpriseUrl, allowedOrganizations, personalAccessToken);
    }
}

