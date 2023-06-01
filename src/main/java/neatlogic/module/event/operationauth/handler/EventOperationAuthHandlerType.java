package neatlogic.module.event.operationauth.handler;

import neatlogic.framework.process.operationauth.core.IOperationAuthHandlerType;
import neatlogic.framework.util.I18nUtils;

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
        return I18nUtils.getMessage(text);
    }
    @Override
    public String getValue() {
        return value;
    }

}
