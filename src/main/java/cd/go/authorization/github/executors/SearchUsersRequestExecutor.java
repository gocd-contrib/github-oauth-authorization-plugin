package cd.go.authorization.github.executors;


import cd.go.authorization.github.GitHubClientBuilder;
import cd.go.authorization.github.models.AuthConfig;
import cd.go.authorization.github.models.User;
import cd.go.authorization.github.utils.Util;
import com.thoughtworks.go.plugin.api.request.GoPluginApiRequest;
import com.thoughtworks.go.plugin.api.response.DefaultGoPluginApiResponse;
import com.thoughtworks.go.plugin.api.response.GoPluginApiResponse;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedSearchIterable;

import java.io.IOException;
import java.util.*;

import static cd.go.authorization.github.GitHubPlugin.LOG;

public class SearchUsersRequestExecutor implements RequestExecutor {

    private static final String SEARCH_TERM = "search_term";
    private GoPluginApiRequest request;
    private GitHubClientBuilder gitHubClientBuilder;

    public SearchUsersRequestExecutor(GoPluginApiRequest request) {
        this(request, new GitHubClientBuilder());
    }

    SearchUsersRequestExecutor(GoPluginApiRequest request, GitHubClientBuilder gitHubClientBuilder) {
        this.request = request;
        this.gitHubClientBuilder = gitHubClientBuilder;
    }

    SearchUsersRequestExecutor(GitHubClientBuilder gitHubClientBuilder) {
        this.gitHubClientBuilder = gitHubClientBuilder;
    }

    @Override
    public GoPluginApiResponse execute() throws Exception {
        String requestJson = request.requestBody();
        Map<String, String> requestParam = Util.GSON.fromJson(requestJson, Map.class);
        String searchTerm = requestParam.get(SEARCH_TERM);
        List<AuthConfig> authConfigs = AuthConfig.fromJSONList(requestJson);

        final Set<User> users = searchUsers(searchTerm, authConfigs);

        return new DefaultGoPluginApiResponse(200, Util.GSON.toJson(users));
    }

    private Set<User> searchUsers(String searchTerm, List<AuthConfig> authConfigs) throws IOException {
        final HashSet<User> users = new HashSet<>();
        if (authConfigs != null || !authConfigs.isEmpty()) {
            for (AuthConfig authConfig : authConfigs) {
                try {
                    LOG.info(String.format("[User Search] Looking up for users matching search_term: `%s`" +
                            " using auth_config: `%s`", searchTerm, authConfig.getId()));
                    users.addAll(search(searchTerm, authConfig));
                } catch (Exception e) {
                    LOG.error(String.format("[User Search] Error while searching users with auth_config: '%s'", authConfig.getId()), e);
                }
            }
        }
        return users;
    }

    private Set<User> search(String searchText, AuthConfig authConfig) throws IOException {
        HashSet<User> users = new HashSet<>();
        long start = System.currentTimeMillis();
        GitHub client = gitHubClientBuilder.build(authConfig.gitHubConfiguration().personalAccessToken(), authConfig.gitHubConfiguration());
        PagedSearchIterable<GHUser> ghUsers = client.searchUsers().q(searchText).list();
        long afterRequest = System.currentTimeMillis();
        LOG.debug("Time for request: " + (afterRequest - start) + "ms");
        PagedSearchIterable<GHUser> listOfUsers = ghUsers.withPageSize(10);
        for (GHUser ghUser : listOfUsers.iterator().nextPage()) {
            users.add(new User(ghUser.getLogin(), ghUser.getName(), ghUser.getEmail()));
        }
        long end = System.currentTimeMillis();
        LOG.debug("Total time: " + (end - start) + "ms");
        return users;
    }
}
