/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.servicebus.stream.binder;

import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusExtendedBindingProperties;
import com.microsoft.azure.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import com.microsoft.azure.servicebus.stream.binder.provisioning.ServiceBusChannelProvisioner;
import com.microsoft.azure.spring.integration.core.DefaultMessageHandler;
import com.microsoft.azure.spring.integration.core.api.SendOperation;
import org.springframework.cloud.stream.binder.*;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.integration.expression.FunctionExpression;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

/**
 * @author Warren Zhu
 */
public abstract class ServiceBusMessageChannelBinder extends
        AbstractMessageChannelBinder<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>, ServiceBusChannelProvisioner>
        implements
        ExtendedPropertiesBinder<MessageChannel, ServiceBusConsumerProperties, ServiceBusProducerProperties> {

    private ServiceBusExtendedBindingProperties bindingProperties = new ServiceBusExtendedBindingProperties();

    public ServiceBusMessageChannelBinder(String[] headersToEmbed, ServiceBusChannelProvisioner provisioningProvider) {
        super(headersToEmbed, provisioningProvider);
    }

    @Override
    protected MessageHandler createProducerMessageHandler(ProducerDestination destination,
            ExtendedProducerProperties<ServiceBusProducerProperties> producerProperties, MessageChannel errorChannel) {
        DefaultMessageHandler handler = new DefaultMessageHandler(destination.getName(), getSendOperation());
        handler.setBeanFactory(getBeanFactory());
        handler.setSync(producerProperties.getExtension().isSync());
        handler.setSendTimeout(producerProperties.getExtension().getSendTimeout());
        if (producerProperties.isPartitioned()) {
            handler.setPartitionKeyExpressionString(
                    "'partitionKey-' + headers['" + BinderHeaders.PARTITION_HEADER + "']");
        } else {
            handler.setPartitionKeyExpression(new FunctionExpression<Message<?>>(m -> m.getPayload().hashCode()));
        }

        return handler;
    }

    @Override
    public ServiceBusConsumerProperties getExtendedConsumerProperties(String channelName) {
        return this.bindingProperties.getExtendedConsumerProperties(channelName);
    }

    @Override
    public ServiceBusProducerProperties getExtendedProducerProperties(String channelName) {
        return this.bindingProperties.getExtendedProducerProperties(channelName);
    }

    @Override
    public String getDefaultsPrefix() {
        return this.bindingProperties.getDefaultsPrefix();
    }

    @Override
    public Class<? extends BinderSpecificPropertiesProvider> getExtendedPropertiesEntryClass() {
        return this.bindingProperties.getExtendedPropertiesEntryClass();
    }

    public void setBindingProperties(ServiceBusExtendedBindingProperties bindingProperties) {
        this.bindingProperties = bindingProperties;
    }

    abstract SendOperation getSendOperation();
}