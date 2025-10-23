/*
 * Copyright 2023 ThoughtWorks, Inc.
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
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class AbstractRequestTest {

    @Mock
    private GoPluginApiRequest mockRequest;

    @BeforeEach
    public void setUp() {
        openMocks(this);
    }

    @Nested
    class FromTests {

        @Test
        void shouldDeserializeFromJsonRequestBody() {
            when(mockRequest.requestBody()).thenReturn("{ \"foo\": \"hello\", \"bar\": \"world\"}");

            ExampleRequest actual = Request.from(mockRequest, ExampleRequest.class);

            assertEquals("hello", actual.getFoo());
            assertEquals("world", actual.getBar());
        }

        @Test
        void shouldDeserializeFromJsonRequestBody_IgnoringAdditionalProperties() {
            when(mockRequest.requestBody()).thenReturn("{ \"foo\": \"hello\", \"bar\": \"world\", \"ignore\": \"me\"}");

            ExampleRequest actual = Request.from(mockRequest, ExampleRequest.class);

            assertEquals("hello", actual.getFoo());
            assertEquals("world", actual.getBar());
        }

        static class ExampleRequest extends Request {

            @Expose
            @SerializedName("foo")
            private String foo;

            @Expose
            @SerializedName("bar")
            private String bar;

            public String getFoo() {
                return foo;
            }

            public String getBar() {
                return bar;
            }

            @Override
            public RequestExecutor executor() {
                return null;
            }
        }

    }

}