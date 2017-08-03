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

package cd.go.authorization.github;

import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHTeam;
import org.kohsuke.github.GitHub;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MembershipCheckerTest {

    private GitHub gitHub;
    private MembershipChecker membershipChecker;

    @Before
    public void setUp() throws Exception {
        gitHub = mock(GitHub.class);

        membershipChecker = new MembershipChecker();
    }

    @Test
    public void isAMemberOfAtLeastOneOrganization_shouldReturnTrueIfUserIsAMemberOfAtLeastOneOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(gitHub.getMyOrganizations()).thenReturn(singletonMap("organization-foo", organization));
        when(organization.getName()).thenReturn("organization-foo");

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(gitHub, asList("organization-foo", "organization-bar"));

        assertTrue(aMemberOfAtLeastOneOrganization);
    }

    @Test
    public void isAMemberOfAtLeastOneOrganization_shouldReturnFalseIfUserIsNotAMemberOfAnyOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);

        when(gitHub.getMyOrganizations()).thenReturn(singletonMap("organization-baz", organization));
        when(organization.getName()).thenReturn("organization-baz");

        final boolean aMemberOfAtLeastOneOrganization = membershipChecker.isAMemberOfAtLeastOneOrganization(gitHub, asList("organization-foo", "organization-bar"));

        assertFalse(aMemberOfAtLeastOneOrganization);
    }

    @Test
    public void isAMemberOfAtLeastOneTeamOfOrganization_shouldReturnTrueIfUserIsAMemberOfAtLeastOneTeamOfOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);
        final GHTeam team = mock(GHTeam.class);

        when(team.getName()).thenReturn("TeamX");
        when(organization.getName()).thenReturn("organization-foo");
        when(gitHub.getMyTeams()).thenReturn(singletonMap("organization-foo", singleton(team)));

        final boolean aMMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(gitHub, singletonMap("organization-foo", asList("teamx")));

        assertTrue(aMMemberOfAtLeastOneTeamOfOrganization);
    }

    @Test
    public void isAMemberOfAtLeastOneTeamOfOrganization_shouldReturnFalseIfUserIsNotAMemberOfAnyTeamOfOrganization() throws Exception {
        final GHOrganization organization = mock(GHOrganization.class);
        final GHTeam team = mock(GHTeam.class);

        when(team.getName()).thenReturn("TeamA");
        when(organization.getName()).thenReturn("organization-foo");
        when(gitHub.getMyTeams()).thenReturn(singletonMap("organization-foo", singleton(team)));

        final boolean aMMemberOfAtLeastOneTeamOfOrganization = membershipChecker.isAMemberOfAtLeastOneTeamOfOrganization(gitHub, singletonMap("organization-foo", asList("TeamX")));

        assertFalse(aMMemberOfAtLeastOneTeamOfOrganization);
    }

}