package codedriver.module.event.dto;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.restful.annotation.EntityField;

public class EventVo {

    @EntityField(name = "事件id", type = ApiParamType.LONG)
    private Long id;
    @EntityField(name = "事件归档类型id", type = ApiParamType.LONG)
    private Long eventTypeId;
    @EntityField(name = "事件解决方案id", type = ApiParamType.LONG)
    private Long eventSolutionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventTypeId() {
        return eventTypeId;
    }

    public void setEventTypeId(Long eventTypeId) {
        this.eventTypeId = eventTypeId;
    }

    public Long getEventSolutionId() {
        return eventSolutionId;
    }

    public void setEventSolutionId(Long eventSolutionId) {
        this.eventSolutionId = eventSolutionId;
    }
    
}
