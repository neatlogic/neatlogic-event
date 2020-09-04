package codedriver.module.event.constvalue;

import codedriver.framework.process.audithandler.core.IProcessTaskAuditDetailType;

public enum EventAuditDetailType implements IProcessTaskAuditDetailType {
    EVENTINFO("eventinfo", "事件", "event", "oldEvent", 16)
    ;
    
    private EventAuditDetailType(String _value, String _text, String _paramName, String _oldDataParamName, int _sort) {
        this.value = _value;
        this.text = _text;
        this.paramName = _paramName;
        this.oldDataParamName = _oldDataParamName;
        this.sort = _sort;
    }
    
    private String value;
    private String text;
    private String paramName;
    private String oldDataParamName;
    private int sort;
    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getParamName() {
        return paramName;
    }

    @Override
    public String getOldDataParamName() {
        return oldDataParamName;
    }

    @Override
    public int getSort() {
        return sort;
    }

}
