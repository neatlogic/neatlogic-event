package codedriver.module.event.operationauth.handler;

import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;

public enum EventOperationAuthHandlerType implements IOperationAuthHandlerType {
    EVENT("事件");
    
    private EventOperationAuthHandlerType(String text) {
        this.text = text;
    }
    
    private String text;
    @Override
    public String getText() {
        return text;
    }

}
