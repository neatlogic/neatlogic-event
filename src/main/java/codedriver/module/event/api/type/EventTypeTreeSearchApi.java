package codedriver.module.event.api.type;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthenticationInfoVo;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.service.AuthenticationInfoService;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventTypeVo;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeTreeSearchApi extends PrivateApiComponentBase {

    @Autowired
    private EventTypeMapper eventTypeMapper;
	@Resource
	private AuthenticationInfoService authenticationInfoService;

    @Override
    public String getToken() {
        return "eventtype/tree/search";
    }

    @Override
    public String getName() {
        return "检索事件类型架构";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param(name = "id", type = ApiParamType.LONG, desc = "主键ID"),
    	@Param(name = "keyword", type = ApiParamType.STRING, xss = true, desc = "关键字"),
        @Param(name = "isAuthenticate", type = ApiParamType.ENUM, desc = "是否需要鉴权", rule = "0,1")
    })
    @Output({
    	@Param(name = "children", type = ApiParamType.JSONARRAY, explode = EventTypeVo[].class,desc = "事件类型架构集合")
    })
    @Description(desc = "检索事件类型架构")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	JSONObject resultObj = new JSONObject();
		resultObj.put("children", new ArrayList<>());
		List<Long> authorizedEventTypeIdList = new ArrayList<>();
		Integer isAuthenticate = jsonObj.getInteger("isAuthenticate");
		if(Objects.equals(isAuthenticate, 1)) {
			AuthenticationInfoVo authenticationInfoVo = authenticationInfoService.getAuthenticationInfo(UserContext.get().getUserUuid(true));
		    authorizedEventTypeIdList = eventTypeMapper.getCurrentUserAuthorizedEventTypeIdList(UserContext.get().getUserUuid(true), authenticationInfoVo.getTeamUuidList(), authenticationInfoVo.getRoleUuidList());
		}
		List<EventTypeVo> eventTypeList = new ArrayList<>();
		Long id = jsonObj.getLong("id");
		String keyword = jsonObj.getString("keyword");
		List<Long> eventTypeIdList = new ArrayList<>();
		Map<Long, EventTypeVo> eventTypeMap = new HashMap<>();
		if(id != null){
			EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(id);
			if(eventTypeVo == null) {
				throw new EventTypeNotFoundException(id);
			}
			if(!Objects.equals(isAuthenticate, 1) || authorizedEventTypeIdList.contains(eventTypeVo.getId())) {
	            eventTypeList = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
	            for(EventTypeVo eventType : eventTypeList) {
	                eventTypeMap.put(eventType.getId(), eventType);
	                eventTypeIdList.add(eventType.getId());
	            }
			}
		}else if(StringUtils.isNotBlank(keyword)){
			EventTypeVo keywordEventType = new EventTypeVo();
			keywordEventType.setKeyword(keyword);
			keywordEventType.setNeedPage(false);
			List<EventTypeVo> targetEventTypeList = eventTypeMapper.searchEventType(keywordEventType);
			targetEventTypeList.sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
			for(EventTypeVo eventTypeVo : targetEventTypeList) {
                if(eventTypeMap.containsKey(eventTypeVo.getId())) {
                    continue;
                }
			    if(!Objects.equals(isAuthenticate, 1) || authorizedEventTypeIdList.contains(eventTypeVo.getId())) {
	                List<EventTypeVo> ancestorsAndSelf = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
	                for(EventTypeVo eventType : ancestorsAndSelf) {
	                    if(!eventTypeIdList.contains(eventType.getId())) {
	                        eventTypeMap.put(eventType.getId(), eventType);
	                        eventTypeIdList.add(eventType.getId());
	                        eventTypeList.add(eventType);
	                    }
	                }
	                /** 把下游的类型也捞取出来，供工单编辑页使用 */
					List<EventTypeVo> children = eventTypeMapper.getChildrenByLeftRightCode(eventTypeVo.getLft(), eventTypeVo.getRht());
					if(CollectionUtils.isNotEmpty(children)){
						for(EventTypeVo eventType : children) {
							if(!eventTypeIdList.contains(eventType.getId())) {
								eventTypeMap.put(eventType.getId(), eventType);
								eventTypeIdList.add(eventType.getId());
								eventTypeList.add(eventType);
							}
						}
					}
				}
			}
		}else if(Objects.equals(isAuthenticate, 1)){
		    if(CollectionUtils.isNotEmpty(authorizedEventTypeIdList)) {
	            List<EventTypeVo> targetEventTypeList = eventTypeMapper.getEventTypeListByIdList(authorizedEventTypeIdList);
	            targetEventTypeList.sort((e1, e2) -> e2.getId().compareTo(e1.getId()));
	            for(EventTypeVo eventTypeVo : targetEventTypeList) {
	                if(eventTypeMap.containsKey(eventTypeVo.getId())) {
	                    continue;
	                }
	                List<EventTypeVo> ancestorsAndSelf = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
                    for(EventTypeVo eventType : ancestorsAndSelf) {
                        if(!eventTypeIdList.contains(eventType.getId())) {
                            eventTypeMap.put(eventType.getId(), eventType);
                            eventTypeIdList.add(eventType.getId());
                            eventTypeList.add(eventType);
                        }
                    }
	            }
		    }
		}else {
			return resultObj;
		}

		if(CollectionUtils.isNotEmpty(eventTypeList)) {
			EventTypeVo rootEventType = new EventTypeVo();
			rootEventType.setId(EventTypeVo.ROOT_ID);
			rootEventType.setName("root");
			rootEventType.setParentId(EventTypeVo.ROOT_PARENTID);
			eventTypeMap.put(EventTypeVo.ROOT_ID, rootEventType);
			Map<Long, EventTypeVo> eventTypeSolutionCountMap = new HashMap<>();
			for(Map.Entry<Long,EventTypeVo> entry : eventTypeMap.entrySet()){
				EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(entry.getValue().getLft(), entry.getValue().getRht());
				count.setId(entry.getKey());
				eventTypeSolutionCountMap.put(entry.getKey(),count);
			}
			List<EventTypeVo> eventTypeChildCountList = eventTypeMapper.getEventTypeChildCountListByIdList(eventTypeIdList);
//			Map<Long, EventTypeVo> eventTypeSolutionCountAndChildCountMap = new HashMap<>();
			for(EventTypeVo eventType : eventTypeChildCountList) {
//				eventTypeSolutionCountAndChildCountMap.put(eventType.getId(), eventType);
				EventTypeVo eventTypeSolutionCount = eventTypeSolutionCountMap.get(eventType.getId());
				EventTypeVo targetEventType = eventTypeMap.get(eventType.getId());
			    if(targetEventType != null) {
			        targetEventType.setChildCount(eventType.getChildCount());
			        if(eventTypeSolutionCount != null){
						targetEventType.setSolutionCount(eventTypeSolutionCount.getSolutionCount());
					}
			    }
			}
			for(EventTypeVo eventType : eventTypeList) {
				EventTypeVo parentEventType = eventTypeMap.get(eventType.getParentId());
				if(parentEventType != null) {
					eventType.setParent(parentEventType);
				}
//				EventTypeVo eventTypeSolutionCountAndChildCount = eventTypeSolutionCountAndChildCountMap.get(eventType.getId());
//				if(eventTypeSolutionCountAndChildCount != null) {
//					eventType.setChildCount(eventTypeSolutionCountAndChildCount.getChildCount());
//					eventType.setSolutionCount(eventTypeSolutionCountAndChildCount.getSolutionCount());
//				}
			}
			resultObj.put("children", rootEventType.getChildren());
		}

    	return resultObj;
    }
}
