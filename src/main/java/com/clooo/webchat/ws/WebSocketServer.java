package com.clooo.webchat.ws;


import com.clooo.webchat.ws.manager.RoomSessionManager;
import com.clooo.webchat.ws.handler.WebSocketServerHandler;
import com.clooo.webchat.ws.manager.ChannelManager;
import com.clooo.webchat.ws.manager.SingleSessionManager;
import com.clooo.webchat.ws.protocol.TransferProtocol;
import com.clooo.webchat.ws.protocol.TransferProtocolCodec;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.stream.ChunkedWriteHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

@Component
@Slf4j
public class WebSocketServer {

    @Value("${server.ssl.key-store}")
    private String keyStorePath;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${server.ssl.key-store-type}")
    private String keyStoreType;
    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    @Value("${server.port}")
    private int serverPort;

    private final ResourceLoader resourceLoader;

    private final int port;
    private final RoomSessionManager roomSessionManager;
    private final ChannelManager channelManager;
    private final SingleSessionManager singleSessionManager;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;


    public WebSocketServer(ResourceLoader resourceLoader,
                           RoomSessionManager roomSessionManager,
                           ChannelManager channelManager,
                           SingleSessionManager singleSessionManager) {
        this.resourceLoader = resourceLoader;
        this.port = 8080;
        this.roomSessionManager = roomSessionManager;
        this.channelManager = channelManager;
        this.singleSessionManager = singleSessionManager;
    }

    @PostConstruct
    public void start() {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WebSocketServerInitializer(sslContext(), roomSessionManager, channelManager, singleSessionManager));
            Channel channel = b.bind("0.0.0.0", port).sync().channel();
           log.info("WebSocket server started at port " + port + '.');
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @PreDestroy
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public SslContext sslContext() {
        try {

            InputStream keyStoreStream = resourceLoader.getResource(keyStorePath).getInputStream();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(keyStoreStream, keyStorePassword.toCharArray());


            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            return SslContextBuilder.forServer(keyManagerFactory).build();
        } catch (IOException | NoSuchAlgorithmException | CertificateException | KeyStoreException |
                 UnrecoverableKeyException e) {
            log.error("证书无效：{}", e.getMessage());
            return null;
        }

    }

    private static class WebSocketServerInitializer extends ChannelInitializer<Channel> {
        private final SslContext sslCtx;

        private final RoomSessionManager roomSessionManager;
        private final ChannelManager channelManager;
        private final SingleSessionManager singleSessionManager;

        public WebSocketServerInitializer(SslContext sslCtx, RoomSessionManager roomSessionManager, ChannelManager channelManager, SingleSessionManager singleSessionManager) {
            this.roomSessionManager = roomSessionManager;
            this.channelManager = channelManager;
            this.singleSessionManager = singleSessionManager;
            // 初始化 SSL 上下文

            if (sslCtx == null) {
                try (InputStream certChainFileStream = getClass().getClassLoader().getResourceAsStream("cert/cert.pem");
                     InputStream keyFileStream = getClass().getClassLoader().getResourceAsStream("cert/key.pem")) {
                    this.sslCtx = SslContextBuilder.forServer(certChainFileStream, keyFileStream).build();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                this.sslCtx = sslCtx;
            }
        }


        @Override
        protected void initChannel(Channel ch) {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
            pipeline.addLast(new HttpServerCodec());
            pipeline.addLast(new HttpObjectAggregator(TransferProtocol.MAX_CONTENT_SIZE));
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new WebSocketServerProtocolHandler("/websocket"));
            pipeline.addLast(new TransferProtocolCodec());
            pipeline.addLast(new WebSocketServerHandler(roomSessionManager, channelManager, singleSessionManager));
        }

    }

}
