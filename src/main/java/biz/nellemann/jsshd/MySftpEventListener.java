package biz.nellemann.jsshd;

import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.sftp.server.Handle;
import org.apache.sshd.sftp.server.SftpEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MySftpEventListener implements SftpEventListener {

    private final static Logger log = LoggerFactory.getLogger(MySftpEventListener.class);

    @Override
    public void open(ServerSession session, String remoteHandle, Handle localHandle) throws IOException {
        log.info("open() - {}", localHandle.toString());
    }


    @Override
    public void closed(ServerSession session, String remoteHandle, Handle localHandle, Throwable thrown) throws IOException {
        log.info("closed() - {}", localHandle.toString());
    }

}
