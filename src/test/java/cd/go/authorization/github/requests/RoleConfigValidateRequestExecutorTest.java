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

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.util.Collections;

import static java.util.Collections.singletonMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RoleConfigValidateRequestExecutorTest {
    private GoPluginApiRequest request;

    @Before
    public void setup() throws Exception {
        request = mock(GoPluginApiRequest.class);
    }

    @Test
    public void shouldValidateEmptyRoleConfig() throws Exception {
        when(request.requestBody()).thenReturn(new Gson().toJson(Collections.emptyMap()));

        GoPluginApiResponse response = RoleConfigValidateRequest.from(request).execute();
        String json = response.responseBody();

        String expectedJSON = "[\n" +
                "  {\n" +
                "    \"key\": \"Users\",\n" +
                "    \"message\": \"At least one of the fields(organizations,teams or users) should be specified.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"Teams\",\n" +
                "    \"message\": \"At least one of the fields(organizations,teams or users) should be specified.\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"key\": \"Organizations\",\n" +
                "    \"message\": \"At least one of the fields(organizations,teams or users) should be specified.\"\n" +
                "  }\n" +
                "]";

        JSONAssert.assertEquals(expectedJSON, json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateRoleConfigWithBadTeamsValueFormat() throws Exception {
        when(request.requestBody()).thenReturn(new Gson().toJson(singletonMap("Teams", "Org")));

        GoPluginApiResponse response = RoleConfigValidateRequest.from(request).execute();
        String json = response.responseBody();

        String expectedJSON = "[\n" +
                "  {\n" +
                "    \"key\": \"Teams\",\n" +
                "    \"message\": \"Invalid format. It should be in <organization>:<team-1>,<team-2> format.\"\n" +
                "  }\n" +
                "]";

        JSONAssert.assertEquals(expectedJSON, json, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void shouldValidateValidRoleConfig() throws Exception {
        when(request.requestBody()).thenReturn(new Gson().toJson(singletonMap("Teams", "Org:team-1")));

        GoPluginApiResponse response = RoleConfigValidateRequest.from(request).execute();
        String json = response.responseBody();

        String expectedJSON = "[]";

        JSONAssert.assertEquals(expectedJSON, json, JSONCompareMode.NON_EXTENSIBLE);
    }

}