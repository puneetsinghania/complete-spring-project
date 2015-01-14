package com.edwise.completespring.entities;

import org.junit.Test;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

// TODO update with enum...

public class UserAccountTest {
    private static final long ID_TEST1 = 123l;
    private static final long ID_TEST2 = 456l;
    private static final String USERNAME_TEST1 = "aragorn1981";
    private static final String USERNAME_TEST2 = "neo_80";
    private static final String PASSWORD_TEST1 = "passwordTest1";

    @Test
    public void testEquals() {
        UserAccount userAccount1 = createUserAccount(ID_TEST1, USERNAME_TEST1, PASSWORD_TEST1);
        UserAccount userAccount2 = createUserAccount(ID_TEST1, USERNAME_TEST1, PASSWORD_TEST1);

        assertTrue(userAccount1.equals(userAccount2) && userAccount2.equals(userAccount1));
    }

    @Test
    public void testNotEqualsWithDifferentsFields() {
        UserAccount userAccount1 = createUserAccount(ID_TEST1, USERNAME_TEST1, null);
        UserAccount userAccount2 = createUserAccount(ID_TEST1, USERNAME_TEST2, null);

        assertFalse(userAccount1.equals(userAccount2) || userAccount2.equals(userAccount1));
    }

    @Test
    public void testNotEqualsWithDifferentsObjects() {
        UserAccount userAccount = createUserAccount(ID_TEST1, null, null);

        assertFalse(userAccount.equals(new Object()));
    }

    @Test
    public void testHashCode() {
        UserAccount userAccount1 = createUserAccount(ID_TEST1, USERNAME_TEST1, PASSWORD_TEST1);
        UserAccount userAccount2 = createUserAccount(ID_TEST1, USERNAME_TEST1, PASSWORD_TEST1);

        assertEquals(userAccount1.hashCode(), userAccount2.hashCode());
    }

    @Test
    public void testHasCodeWithDifferentFields() {
        UserAccount userAccount1 = createUserAccount(ID_TEST1, USERNAME_TEST1, PASSWORD_TEST1);
        UserAccount userAccount2 = createUserAccount(ID_TEST2, USERNAME_TEST2, PASSWORD_TEST1);

        assertNotEquals(userAccount1.hashCode(), userAccount2.hashCode());
    }

    @Test
    public void testToString() {
        UserAccount userAccount = createUserAccount(null, null, null);
        String userAccountString = userAccount.toString();

        assertThatUserAccountStringContainsAllFields(userAccountString);
    }

    private void assertThatUserAccountStringContainsAllFields(String userAccountString) {
        assertThat(userAccountString, containsString("id=null"));
        assertThat(userAccountString, containsString("username=null"));
        assertThat(userAccountString, containsString("password=null"));
    }

    public static UserAccount createUserAccount(Long id, String username, String password) {
        return new UserAccount()
                .setId(id)
                .setUsername(username)
                .setPassword(password);
    }
}