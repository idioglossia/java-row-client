package io.ep2p.row.client.tyrus;

import io.ep2p.row.client.callback.ResponseCallback;
import io.ep2p.row.client.model.RowRequest;
import io.ep2p.row.client.model.protocol.Naming;
import io.ep2p.row.client.ws.ContainerFactory;
import io.ep2p.row.client.ws.RowClientEndpointConfig;
import io.ep2p.row.client.ws.RowWebsocketHandlerAdapter;
import io.ep2p.row.client.ws.RowWebsocketSession;
import io.ep2p.row.client.RequestSender;
import io.ep2p.row.client.RowClient;
import io.ep2p.row.client.RowClientConfig;
import io.ep2p.row.client.RowMessageHandler;
import io.ep2p.row.client.callback.SubscriptionListener;
import lombok.SneakyThrows;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.concurrent.Callable;

public class TyrusRowWebsocketClient implements RowClient {
    private final RequestSender requestSender;
    private final RowClientConfig<RowWebsocketSession> rowClientConfig;
    private volatile RowWebsocketSession webSocketSession;

    public TyrusRowWebsocketClient(RowClientConfig<RowWebsocketSession> rowClientConfig) {
        this.requestSender = new RequestSender(rowClientConfig.getConnectionRepository(), rowClientConfig.getMessageIdGenerator(), rowClientConfig.getCallbackRegistry(), rowClientConfig.getSubscriptionListenerRegistry(), rowClientConfig.getMessageConverter());
        this.rowClientConfig = rowClientConfig;
    }

    public void sendRequest(RowRequest<?, ?> rowRequest, ResponseCallback<?> callback) throws IOException {
        requestSender.sendRequest(rowRequest, callback);
    }

    public void subscribe(RowRequest<?, ?> rowRequest, ResponseCallback<?> callback, SubscriptionListener<?> subscriptionListener) throws IOException {
        requestSender.sendSubscribe(rowRequest, callback,subscriptionListener);
    }

    @SneakyThrows
    public void open() {
        WebSocketContainer webSocketContainer = ContainerFactory.getWebSocketContainer(rowClientConfig.getWebsocketConfig());
        URI uri = URI.create(rowClientConfig.getAddress());
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().configurator(new RowClientEndpointConfig(rowClientConfig.getHandshakeHeadersProvider().getHeaders())).preferredSubprotocols(Collections.singletonList(Naming.ROW_PROTOCOL_NAME)).extensions(Collections.<Extension>emptyList()).build();
        RowMessageHandler<RowWebsocketSession> rowMessageHandler = rowClientConfig.getRowMessageHandlerProvider().provide(rowClientConfig, this);
        this.webSocketSession = new RowWebsocketSession(rowClientConfig.getAttributes(), uri, rowClientConfig.getWebsocketConfig());
        Endpoint endpoint = new RowWebsocketHandlerAdapter(this.webSocketSession, rowMessageHandler);
        Callable<Void> callableContainer = getCallableContainer(webSocketContainer, uri, clientEndpointConfig, endpoint);
        if(this.rowClientConfig.getExecutorService() != null){
            this.rowClientConfig.getExecutorService().submit(callableContainer);
        }else {
            callableContainer.call();
        }
    }

    private Callable<Void> getCallableContainer(WebSocketContainer webSocketContainer, URI uri, ClientEndpointConfig clientEndpointConfig, Endpoint endpoint){
        return new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                webSocketContainer.connectToServer(endpoint, clientEndpointConfig, uri);
                return null;
            }
        };
    }

    @SneakyThrows
    public synchronized void close() {
        if (this.webSocketSession != null) {
            this.webSocketSession.close();
            this.webSocketSession = null;
        }
    }
}
