package labs.psychogen.row.client.ws;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.WebSocketContainer;

public class ContainerFactory {
    public static WebSocketContainer getWebSocketContainer(WebsocketConfig websocketConfig){
        ClientManager clientManager = new ClientManager();
        clientManager.setAsyncSendTimeout(websocketConfig.getAsyncSendTimeout());
        clientManager.setDefaultMaxSessionIdleTimeout(websocketConfig.getMaxSessionIdleTimeout());
        clientManager.setDefaultMaxBinaryMessageBufferSize(websocketConfig.getMaxBinaryMessageBufferSize());
        clientManager.setDefaultMaxTextMessageBufferSize(websocketConfig.getMaxBinaryMessageBufferSize());
        return clientManager;
    }
}
