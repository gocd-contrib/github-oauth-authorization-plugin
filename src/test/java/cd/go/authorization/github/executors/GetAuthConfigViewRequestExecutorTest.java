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

import cd.go.authorization.github.annotation.MetadataHelper;
import cd.go.authorization.github.annotation.ProfileMetadata;
import cd.go.authorization.github.models.GitHubConfiguration;
import cd.go.authorization.github.utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Map;

import static java.lang.String.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class GetAuthConfigViewRequestExecutorTest {

    @Test
    public void shouldRenderTheTemplateInJSON() {
        GoPluginApiResponse response = new GetAuthConfigViewRequestExecutor().execute();
        assertThat(response.responseCode(), is(200));
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> hashSet = new Gson().fromJson(response.responseBody(), type);
        assertThat(hashSet, hasEntry("template", Util.readResource("/auth-config.template.html")));
    }

    @Test
    public void allFieldsShouldBePresentInView() {
        String template = Util.readResource("/auth-config.template.html");

        final Document document = Jsoup.parse(template);

        for (ProfileMetadata field : MetadataHelper.getMetadata(GitHubConfiguration.class)) {
            final Elements inputFieldForKey = document.getElementsByAttributeValue("ng-model", field.getKey());
            int elementCount = field.getKey().equalsIgnoreCase("AuthenticateWith") ? 2 : 1;
            assertThat(format("Should have only one ng-model for %s", inputFieldForKey), inputFieldForKey, hasSize(elementCount));

            final Elements spanToShowError = document.getElementsByAttributeValue("ng-class", "{'is-visible': GOINPUTNAME[" + field.getKey() + "].$error.server}");
            assertThat(spanToShowError, hasSize(1));
            assertThat(spanToShowError.attr("ng-show"), is("GOINPUTNAME[" + field.getKey() + "].$error.server"));
            assertThat(spanToShowError.text(), is("{{GOINPUTNAME[" + field.getKey() + "].$error.server}}"));
        }

        final Elements inputs = document.select("textarea,input,select");
        //AuthenticateWith is coming twice as it is radio button
        assertThat(inputs, hasSize(MetadataHelper.getMetadata(GitHubConfiguration.class).size() + 1));
    }
}