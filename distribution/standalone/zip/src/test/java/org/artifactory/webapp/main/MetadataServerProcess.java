package org.artifactory.webapp.main;

import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.artifactory.common.ArtifactoryHome;
import org.artifactory.webapp.WebappUtils;
import org.jfrog.client.http.CloseableHttpClientDecorator;
import org.jfrog.client.http.HttpBuilder;
import org.jfrog.common.platform.test.helpers.ProcessUtils;
import org.jfrog.metadata.client.exception.MetadataClientException;
import org.jfrog.metadata.client.http.MetadataHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.artifactory.webapp.main.AccessProcess.TEST_JFROG_JOIN_KEY;
import static org.jfrog.common.FileUtils.createDirectory;

/**
 * @author Uriah Levy
 * Metadata Server process wrapper. For development use only.
 */
public class MetadataServerProcess {

    private File metadataServerHome;
    private StartedProcess process;
    private int port;
    private int accessPort;


    public MetadataServerProcess(File metadataServerHome, AccessProcess accessProcess,
            ArtifactoryHome internalHome, int port) {
        this.accessPort = accessProcess.getPort();
        this.port = port;
        if (metadataServerHome != null) {
            this.metadataServerHome = metadataServerHome;
        } else {
            this.metadataServerHome = new File(System.getProperty("user.home") + "/.metadata");
        }
        createDirectory(Objects.requireNonNull(metadataServerHome));
        saveMdsBootstrapFileAndJoinKey();
        copyMasterKeyToMds(internalHome.getHomeDir().getAbsolutePath());
    }

    MetadataServerProcess(int port, int accessPort, String artiHomePath) {
        this.port = port;
        this.accessPort = accessPort;
        this.metadataServerHome = new File(artiHomePath, "metadata");
        createDirectory(Objects.requireNonNull(metadataServerHome));
        saveMdsBootstrapFileAndJoinKey();
        copyMasterKeyToMds(artiHomePath);
    }

    private void copyMasterKeyToMds(String artiHomePath) {
        try {
            File sourceMasterKeyFile = new File(artiHomePath, "/etc/security/master.key");
            if (sourceMasterKeyFile.exists()) {
                FileUtils.copyFile(sourceMasterKeyFile,
                        new File(metadataServerHome, "etc/keys/master.key"));
            }
        } catch (IOException e) {
            log.error("Unable to copy master.key file from Access to MDS", e);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(MetadataServerProcess.class);

    private void saveMdsBootstrapFileAndJoinKey() {
        try {
            FileUtils.writeStringToFile(new File(metadataServerHome, "etc/bootstrap.json"),
                    "{\"master_key\":\"" + metadataServerHome.getAbsolutePath() + "/etc/keys/master.key\"}");
        } catch (IOException e) {
            log.error("Unable to save the MDS bootstrap.json", e);
        }
    }

    public void cleanMdsDb() {
        FileUtils.deleteQuietly(new File(metadataServerHome + "/data/sqlite.db"));
    }

    public void start() throws IOException {
        List<String> cmd = buildCommand();
        port = ProcessUtils.resolvePort(port);
        log.info("Starting external MDS process with port {}", port);
        String mdsPath = metadataServerHome.getAbsolutePath();

        FileUtils.write(new File(mdsPath + "/etc/security/join.key"), TEST_JFROG_JOIN_KEY);
        process = new ProcessExecutor()
                .command(cmd)
                .environment("METADATA_ACCESSCLIENT_URL", "https://localhost:" + accessPort)
                .environment("METADATA_HOME", mdsPath)
                .environment("METADATA_SERVER_PORT", String.valueOf(port))
                .redirectOutput(Slf4jStream.of(log).asInfo())
                .redirectError(Slf4jStream.of(log).asError())
                .destroyOnExit()
                .start();
        if (!waitForStartup()) {
            throw new IllegalStateException("Metadata Server did not start.");
        }
        log.info("Metadata Server process start with PID {}", "unknown :D");
    }

    public void stop() {
        if (process != null && process.getProcess().isAlive()) {
            process.getProcess().destroyForcibly();
        }
    }

    private List<String> buildCommand() {
        return Lists.newArrayList(WebappUtils.getMetadataApiExecutable(SystemUtils.IS_OS_MAC).getAbsolutePath());
    }

    private boolean waitForStartup() {
        long start = System.currentTimeMillis();
        int retries = 0;
        final int maxTime = 20_000;
        while (System.currentTimeMillis() - start < maxTime) {
            if (isProcessAlive()) {
                log.info("Finished waiting for Metadata process after {} millis with {} ping retries",
                        System.currentTimeMillis() - start, retries);
                return true;
            } else if (process.getFuture().isDone()) {
                log.warn("Metadata process exited.");
                return false;
            }
            retries++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        String message = "Metadata is not responding after " + maxTime + " millis";
        log.error(message);
        return false;
    }

    private boolean isProcessAlive() {
        try (MetadataHttpClient client = new MetadataHttpClient(buildHttpClient(), "http://localhost:" + port, null)) {
            client.adHocPing();
            return true;
        } catch (MetadataClientException e) {
            log.debug("Unable to ping metadata server: {}", e.getMessage());
        }
        return false;
    }

    private static CloseableHttpClientDecorator buildHttpClient() {
        return (CloseableHttpClientDecorator) new HttpBuilder()
                .connectionTimeout(2000)
                .socketTimeout(10000)
                .noHostVerification(true)
                .trustSelfSignCert(true)
                .build();
    }

    public int getPort() {
        return port;
    }

    public String getMdsUrl() {
        return "http://localhost:" + port;
    }
}