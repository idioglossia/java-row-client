package io.ep2p.row.client;

import io.ep2p.row.client.callback.RowTransportListener;
import io.ep2p.row.client.exception.MessageDataProcessingException;
import io.ep2p.row.client.pipeline.StoppablePipeline;
import io.ep2p.row.client.ws.MessageHandler;
import io.ep2p.row.client.ws.WebsocketSession;
import io.ep2p.row.client.ws.handler.MessageHandlerInput;

import javax.websocket.CloseReason;

public class RowMessageHandler<S extends WebsocketSession> implements MessageHandler<S> {
    private final StoppablePipeline<MessageHandlerInput, Void> pipeline;
    private final ConnectionRepository<S> connectionRepository;
    private final RowTransportListener<S> rowTransportListener;
    private final RowClient rowClient;

    public RowMessageHandler(StoppablePipeline<MessageHandlerInput, Void> pipeline, ConnectionRepository<S> connectionRepository, RowTransportListener<S> rowTransportListener, RowClient rowClient) {
        this.pipeline = pipeline;
        this.connectionRepository = connectionRepository;
        this.rowTransportListener = rowTransportListener;
        this.rowClient = rowClient;
    }

    public void onOpen(S rowWebsocketSession) {
        connectionRepository.setConnection(rowWebsocketSession);
        rowTransportListener.onOpen(rowWebsocketSession);
    }

    public void onMessage(S rowWebsocketSession, String text) {
        try {
            pipeline.execute(new MessageHandlerInput(text), null);
        } catch (MessageDataProcessingException e) {
            rowTransportListener.onError(rowWebsocketSession, e);
        }
    }

    public void onError(S rowWebsocketSession, Throwable throwable) {
        rowTransportListener.onError(rowWebsocketSession, throwable);
    }

    public void onClose(S rowWebsocketSession, CloseReason closeReason) {
        rowTransportListener.onClose(this.rowClient, rowWebsocketSession, closeReason);
    }
}
