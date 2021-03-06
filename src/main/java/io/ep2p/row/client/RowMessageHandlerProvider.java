package io.ep2p.row.client;

import io.ep2p.row.client.ws.WebsocketSession;
import io.ep2p.row.client.ws.handler.PipelineFactory;

public interface RowMessageHandlerProvider<S extends WebsocketSession> {
    RowMessageHandler<S> provide(RowClientConfig<S> clientConfig, RowClient rowClient);

    class Default<S extends WebsocketSession> implements RowMessageHandlerProvider<S>{

        @Override
        public RowMessageHandler<S> provide(RowClientConfig<S> clientConfig, RowClient rowClient) {
            return new RowMessageHandler<S>(PipelineFactory.getPipeline(clientConfig), clientConfig.getConnectionRepository(), clientConfig.getRowTransportListener(), rowClient);
        }
    }
}
