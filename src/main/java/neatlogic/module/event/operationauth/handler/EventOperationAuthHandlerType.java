package neatlogic.module.event.operationauth.handler;

import neatlogic.framework.process.operationauth.core.IOperationAuthHandlerType;
import neatlogic.framework.util.$;

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
        return $.t(text);
    }
    @Override
    public String getValue() {
        return value;
    }

}
