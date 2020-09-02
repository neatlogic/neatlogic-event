package codedriver.module.event.dao.mapper;

import codedriver.module.event.dto.EventVo;
import codedriver.module.event.dto.ProcessTaskStepEventVo;

public interface EventMapper {

    public Long getEventIdByProcessTaskStepId(Long id);

    public EventVo getEventById(Long eventId);

    public int updateEvent(EventVo eventVo);

    public int insertEvent(EventVo eventVo);

    public int insetProcessTaskStepEvent(ProcessTaskStepEventVo processTaskStepEventVo);

}
