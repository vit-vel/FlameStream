package com.spbsu.datastream.core.application;

import org.apache.zookeeper.server.ServerConfig;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.apache.zookeeper.server.quorum.QuorumPeerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ZooKeeperApplication {
  private final Logger LOG = LoggerFactory.getLogger(ZooKeeperApplication.class);

  public static void main(final String... args) throws IOException {
    new ZooKeeperApplication().run();
  }

  private void run() throws IOException {
    final QuorumPeerConfig quorumConfig = new QuorumPeerConfig();

    try (InputStream stream = getClass().getClassLoader().getResourceAsStream("zookeeper-dev.properties")) {
      final Properties props = new Properties();
      props.load(stream);
      quorumConfig.parseProperties(props);
    } catch (QuorumPeerConfig.ConfigException | IOException e) {
      throw new RuntimeException(e);
    }

    final ZooKeeperServerMain zooKeeperServer = new ZooKeeperServerMain();
    final ServerConfig serverConfig = new ServerConfig();
    serverConfig.readFrom(quorumConfig);

    new Thread(() -> {
      try {
        zooKeeperServer.runFromConfig(serverConfig);
        LOG.info("ZooKeeper is alive");
      } catch (IOException e) {
        LOG.error("ZooKeeper is dead", e);
      }
    }).start();
  }
}