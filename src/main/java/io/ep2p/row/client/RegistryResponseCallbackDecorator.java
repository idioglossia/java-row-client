package io.ep2p.row.client;

import io.ep2p.row.client.callback.ResponseCallback;
import io.ep2p.row.client.callback.ResponseCallbackDecorator;
import io.ep2p.row.client.callback.SubscriptionListener;
import io.ep2p.row.client.model.RowResponse;
import io.ep2p.row.client.model.protocol.Naming;
import io.ep2p.row.client.registry.SubscriptionListenerRegistry;

public class RegistryResponseCallbackDecorator<E> extends ResponseCallbackDecorator<E> {
    private final SubscriptionListenerRegistry subscriptionListenerRegistry;
    private final SubscriptionListener<?> subscriptionListener;

    public RegistryResponseCallbackDecorator(ResponseCallback<E> responseCallback, SubscriptionListenerRegistry subscriptionListenerRegistry, SubscriptionListener<?> subscriptionListener) {
        super(responseCallback);
        this.subscriptionListenerRegistry = subscriptionListenerRegistry;
        this.subscriptionListener = subscriptionListener;
    }

    @Override
    public void onResponse(RowResponse<E> rowResponse) {
        if(rowResponse.getSubscription() != null){
            subscriptionListenerRegistry.registerListener(rowResponse.getSubscription().eventName(), new SubscriptionListenerRegistry.SubscriptionRegistryModel<>(rowResponse.getSubscription(), subscriptionListener));
        }
        super.onResponse(rowResponse);
    }

    private boolean isNewSubscription(RowResponse<E> rowResponse){
        return rowResponse.getHeaders().containsKey(Naming.SUBSCRIPTION_Id_HEADER_NAME);
    }

    private String getSubscriptionEventName(RowResponse<E> rowResponse){
        return rowResponse.getHeaders().get(Naming.SUBSCRIPTION_EVENT_HEADER_NAME);
    }
}
