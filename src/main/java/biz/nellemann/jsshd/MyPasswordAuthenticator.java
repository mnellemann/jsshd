package biz.nellemann.jsshd;

import org.apache.sshd.server.auth.AsyncAuthException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.password.PasswordChangeRequiredException;
import org.apache.sshd.server.session.ServerSession;

public class MyPasswordAuthenticator implements PasswordAuthenticator {

    private final String user;
    private final String pass;

    public MyPasswordAuthenticator(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    @Override
    public boolean authenticate(String username, String password, ServerSession session) throws PasswordChangeRequiredException, AsyncAuthException {
        return username.equals(this.user) && password.equals(this.pass);
    }

}
