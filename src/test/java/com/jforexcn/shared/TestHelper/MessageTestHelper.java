package com.jforexcn.shared.TestHelper;

import com.dukascopy.api.IMessage;
import com.dukascopy.api.IOrder;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by simple on 12/12/16.
 */

public class MessageTestHelper implements IMessage {

    private Type type;
    private Set<Reason> reasons = new HashSet<>();
    private String content;
    private IOrder order;
    private long creationTime = 0;

    public static class MessageBuilder {
        private Type type;
        private Set<Reason> reasons = new HashSet<>();
        private String content;
        private IOrder order;
        private long creationTime = 0;

        public MessageBuilder setType(Type type) {
            this.type = type;
            return this;
        }

        public MessageBuilder setReasons(Set<Reason> reasons) {
            this.reasons = reasons;
            return this;
        }

        public MessageBuilder setContent(String content) {
            this.content = content;
            return this;
        }

        public MessageBuilder setOrder(IOrder order) {
            this.order = order;
            return this;
        }

        public MessageBuilder setCreationTime(long creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public MessageTestHelper create() {
            return new MessageTestHelper(
                    this.type,
                    this.content,
                    this.order,
                    this.creationTime
            );
        }
    }

    public MessageTestHelper(Type type, String content, IOrder order, long creationTime) {
        this.type = type;
        this.content = content;
        this.order = order;
        this.creationTime = creationTime;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Set<Reason> getReasons() {
        return reasons;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public IOrder getOrder() {
        return order;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }
}
