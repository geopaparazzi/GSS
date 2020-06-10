package com.hydrologis.kukuratus.gss;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import spark.embeddedserver.jetty.EmbeddedJettyFactory;
import spark.embeddedserver.jetty.JettyServerFactory;

/**
 * A Factory builder class to create custom Jetty instances.
 */
public class EmbeddedJettyFactoryConstructor {
    /**
     * Creater a http instance.
     * 
     * @param port the port to use.
     * @return the factory.
     */
    static EmbeddedJettyFactory create(int port) {
        return createSsl(port, null, null);
    }

    /**
     * Create a https instance.
     * 
     * @param port         the port to use.
     * @param keystorePath the path to the keystore for the ssl.
     * @param pwd          the keystore password.
     * @return the factory.
     */
    static EmbeddedJettyFactory createSsl(int port, String keystorePath, String pwd) {
        return new EmbeddedJettyFactory(new JettyServerFactory() {
            @Override
            public Server create(int maxThreads, int minThreads, int threadTimeoutMillis) {
                Server server;
                if (maxThreads > 0) {
                    int max = maxThreads > 0 ? maxThreads : 200;
                    int min = minThreads > 0 ? minThreads : 8;
                    int idleTimeout = threadTimeoutMillis > 0 ? threadTimeoutMillis : '\uea60';
                    server = new Server(new QueuedThreadPool(max, min, idleTimeout));
                } else {
                    server = new Server();
                }
                addSsl(server, port, keystorePath, pwd);
                return server;
            }

            @Override
            public Server create(ThreadPool threadPool) {
                Server server = new Server(threadPool);
                addSsl(server, port, keystorePath, pwd);
                return server;
            }

            void addSsl(Server server, int port, String keystorePath, String pwd) {

                if (keystorePath != null && pwd != null) {
                    HttpConfiguration https = new HttpConfiguration();
                    https.addCustomizer(new SecureRequestCustomizer());

                    SslContextFactory sslContextFactory = new SslContextFactory.Server();
                    sslContextFactory.setKeyStorePath(keystorePath);
                    sslContextFactory.setKeyStorePassword(pwd);
                    // sslContextFactory.setKeyManagerPassword("123456");
                    ServerConnector sslConnector = new ServerConnector(server,
                            new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(https));
                    sslConnector.setPort(443);
                    ServerConnector connector = new ServerConnector(server);
                    connector.setPort(port);
                    server.setConnectors(new Connector[] { sslConnector, connector });
                } else {
                    ServerConnector connector = new ServerConnector(server);
                    connector.setPort(port);
                    server.setConnectors(new Connector[] { connector });
                }
            }
        });
    }
}