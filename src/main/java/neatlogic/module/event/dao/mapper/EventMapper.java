package neatlogic.module.event.dao.mapper;

import neatlogic.framework.event.dto.EventVo;
import neatlogic.framework.event.dto.ProcessTaskStepEventVo;

public interface EventMapper {

    public Long getEventIdByProcessTaskStepId(Long id);

    public EventVo getEventById(Long eventId);

    public int updateEvent(EventVo eventVo);

    public int insertEvent(EventVo eventVo);

    public int insetProcessTaskStepEvent(ProcessTaskStepEventVo processTaskStepEventVo);

}
