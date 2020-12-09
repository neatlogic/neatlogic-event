package codedriver.module.event.operationauth.handler;

import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;

public enum EventOperationAuthHandlerType implements IOperationAuthHandlerType {
    EVENT("event", "事件");
    
    private EventOperationAuthHandlerType(String value, String text) {
        this.value = value;
        this.text = text;
    }
    
    private String value;
    private String text;
    @Override
    public String getText() {
        return text;
    }
    @Override
    public String getValue() {
        return value;
    }

}
