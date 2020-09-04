package codedriver.module.event.audithandler.handler;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;

import codedriver.framework.process.audithandler.core.ProcessTaskStepAuditDetailHandlerBase;
import codedriver.framework.process.dto.ProcessTaskStepAuditDetailVo;
import codedriver.module.event.constvalue.EventAuditDetailType;
import codedriver.module.event.dao.mapper.EventSolutionMapper;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventSolutionVo;
import codedriver.module.event.dto.EventTypeVo;
import codedriver.module.event.dto.EventVo;
@Service
public class EventInfoAuditHandler extends ProcessTaskStepAuditDetailHandlerBase {

    @Autowired
    private EventSolutionMapper eventSolutionMapper;
    @Autowired
    private EventTypeMapper eventTypeMapper;
    
	@Override
	public String getType() {
		return EventAuditDetailType.EVENTINFO.getValue();
	}

	@Override
	protected int myHandle(ProcessTaskStepAuditDetailVo processTaskStepAuditDetailVo) {
		if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getNewContent())) {
		    JSONObject eventTypeNamePathObj = new JSONObject();
            eventTypeNamePathObj.put("type", "eventTypeNamePath");
            eventTypeNamePathObj.put("typeName", "归档类型");
		    JSONObject eventSolutionNameObj = new JSONObject();
            eventSolutionNameObj.put("type", "eventSolutionName");
            eventSolutionNameObj.put("typeName", "解决方案");
		    
            JSONArray eventArray = new JSONArray();
			EventVo eventVo = JSON.parseObject(processTaskStepAuditDetailVo.getNewContent(), new TypeReference<EventVo>(){});
			if(eventVo.getEventTypeId() != null || StringUtils.isNotBlank(eventVo.getEventTypeNamePath())) {
			    String eventTypeNamePath = eventVo.getEventTypeNamePath();
			    if(eventVo.getEventTypeId() != null) {
			        EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(eventVo.getEventTypeId());
		            if(eventTypeVo != null) {
		                List<EventTypeVo> eventTypeList = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
		                List<String> eventTypeNameList = eventTypeList.stream().map(EventTypeVo::getName).collect(Collectors.toList());
		                eventTypeNamePath = String.join("/", eventTypeNameList);
		                eventVo.setEventTypeNamePath(eventTypeNamePath);
		            }
			    }
			    eventTypeNamePathObj.put("newContent", eventTypeNamePath);
	            eventArray.add(eventTypeNamePathObj);
			}
			boolean isShowEventSolutionName = false;
			if(eventVo.getEventSolutionId() != null || StringUtils.isNotBlank(eventVo.getEventSolutionName())) {
			    String eventSolutionName = eventVo.getEventSolutionName();
			    if(eventVo.getEventSolutionId() != null) {
			        EventSolutionVo eventSolutionVo = eventSolutionMapper.getSolutionById(eventVo.getEventSolutionId());
	                if(eventSolutionVo != null) {
	                    eventSolutionName = eventSolutionVo.getName();
	                }
			    }
			    isShowEventSolutionName = true;
			    eventSolutionNameObj.put("newContent", eventSolutionName);
			}

			if(StringUtils.isNotBlank(processTaskStepAuditDetailVo.getOldContent())) {
			    EventVo oldEventVo = JSON.parseObject(processTaskStepAuditDetailVo.getOldContent(), new TypeReference<EventVo>(){});		
				if(oldEventVo.getEventTypeId() != null && !oldEventVo.getEventTypeId().equals(eventVo.getEventTypeId())) {
	                if(oldEventVo.getEventTypeId() != null) {
	                    EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(oldEventVo.getEventTypeId());
	                    if(eventTypeVo != null) {
	                        List<EventTypeVo> eventTypeList = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
	                        List<String> eventTypeNameList = eventTypeList.stream().map(EventTypeVo::getName).collect(Collectors.toList());
	                        eventTypeNamePathObj.put("oldContent", String.join("/", eventTypeNameList));
	                    }
	                }
				}else if(StringUtils.isNotBlank(oldEventVo.getEventTypeNamePath()) && oldEventVo.getEventTypeNamePath().equals(eventVo.getEventTypeNamePath())) {
				    eventTypeNamePathObj.put("oldContent", oldEventVo.getEventTypeNamePath());
				}

                if(oldEventVo.getEventSolutionId() != null && !oldEventVo.getEventSolutionId().equals(eventVo.getEventSolutionId())) {
                    EventSolutionVo eventSolutionVo = eventSolutionMapper.getSolutionById(oldEventVo.getEventSolutionId());
                    if(eventSolutionVo != null) {
                        isShowEventSolutionName = true;
                        eventSolutionNameObj.put("oldContent", eventSolutionVo.getName());
                    }
				}else if(StringUtils.isNotBlank(oldEventVo.getEventSolutionName()) && !oldEventVo.getEventSolutionName().equals(eventVo.getEventSolutionName())) {
				    isShowEventSolutionName = true;
				    eventSolutionNameObj.put("oldContent", oldEventVo.getEventSolutionName());
				}
			}
			if(isShowEventSolutionName) {
	            eventArray.add(eventSolutionNameObj);
			}
			processTaskStepAuditDetailVo.setOldContent(null);
			processTaskStepAuditDetailVo.setNewContent(JSON.toJSONString(eventArray));
		}
		return 1;
	}

}
