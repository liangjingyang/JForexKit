package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.IOrder;
import com.dukascopy.api.IStrategy;
import com.dukascopy.api.JFException;

/**
 * Created by simple on 12/12/16.
 */

public class BaseTestHelper {
    private IStrategy strategy;
    private MessageTestHelper.MessageBuilder messageBuilder = new MessageTestHelper.MessageBuilder();

    public IStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(IStrategy strategy) {
        this.strategy = strategy;
    }

    public MessageTestHelper.MessageBuilder getMessageBuilder() {
        return messageBuilder;
    }

    public void setMessageBuilder(MessageTestHelper.MessageBuilder messageBuilder) {
        this.messageBuilder = messageBuilder;
    }

    public static void onStart(IStrategy strategy, ContextTestHelper contextTestHelper) throws JFException {
        EngineTestHelper engineTestHelper = contextTestHelper.getEngine();
        engineTestHelper.setStrategy(strategy);
        for (IOrder order : engineTestHelper.getOrders()) {
            ((OrderTestHelper) order).setStrategy(strategy);
        }
        strategy.onStart(contextTestHelper);
    }


}
