package cd.go.authorization.github.executors;

import cd.go.authorization.github.GitHubClientBuilder;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.*;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class SearchUsersRequestExecutorTest {

    private GoPluginApiRequest request;
    private GitHubClientBuilder clientBuilder;
    private GitHub gitHub;
    private GHUserSearchBuilder userSearchBuilder;

    @Before
    public void setUp() throws Exception {
        clientBuilder = mock(GitHubClientBuilder.class);
        request = mock(GoPluginApiRequest.class);
        gitHub = mock(GitHub.class);
        userSearchBuilder = mock(GHUserSearchBuilder.class);
    }

    @Test
    public void shouldSearchForUsersThatMatchTheSearchTerm() throws Exception {
        SearchUsersRequestExecutor searchUsersRequestExecutor = new SearchUsersRequestExecutor(request, clientBuilder);
        when(request.requestBody()).thenReturn(requestJson());
        when(clientBuilder.build(eq("personalAccessToken"), any())).thenReturn(gitHub);
        when(gitHub.searchUsers()).thenReturn(userSearchBuilder);
        when(userSearchBuilder.q("tom")).thenReturn(userSearchBuilder);
        PagedSearchIterable pageSearchIterable = mock(PagedSearchIterable.class);
        PagedIterator pagedIterator = mock(PagedIterator.class);
        GHUser githubUser = mock(GHUser.class);
        when(userSearchBuilder.list()).thenReturn(pageSearchIterable);
        when(pageSearchIterable.withPageSize(10)).thenReturn(pageSearchIterable);
        when(pageSearchIterable.iterator()).thenReturn(pagedIterator);
        when(pagedIterator.nextPage()).thenReturn(Arrays.asList(githubUser));
        when(githubUser.getLogin()).thenReturn("tom01");
        when(githubUser.getName()).thenReturn("Tom NoLastname");
        when(githubUser.getEmail()).thenReturn("tom@gocd.org");
        GoPluginApiResponse response = searchUsersRequestExecutor.execute();

        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[{\"username\":\"tom01\", \"display_name\": \"Tom NoLastname\", \"email\": \"tom@gocd.org\"}]", response.responseBody(), true);
    }

    @Test
    public void shouldNotPerformSearchIfAuthConfigsIsEmpty() throws Exception {
        SearchUsersRequestExecutor searchUsersRequestExecutor = new SearchUsersRequestExecutor(request, clientBuilder);
        when(request.requestBody()).thenReturn(requestJsonWithEmptyAuthConfigs());
        GoPluginApiResponse response = searchUsersRequestExecutor.execute();

        verify(clientBuilder, never()).build(anyString(), any());
        assertThat(response.responseCode(), is(200));
        JSONAssert.assertEquals("[]", response.responseBody(), false);
    }


    private String requestJson() {
        return "{\n" +
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
                "}";
    }

    private String requestJsonWithEmptyAuthConfigs() {
        return "{\n" +
                "  \"search_term\": \"tom\",\n" +
                "  \"auth_configs\": []\n" +
                "}";
    }

}