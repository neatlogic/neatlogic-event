package codedriver.module.event.api.solution;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.dao.mapper.EventSolutionMapper;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventTypeVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeTreeForSolutionApi extends PrivateApiComponentBase {

    @Autowired
    private EventTypeMapper eventTypeMapper;

	@Autowired
	private EventSolutionMapper eventSolutionMapper;

    @Override
    public String getToken() {
        return "event/solution/tree";
    }

    @Override
    public String getName() {
        return "查询解决方案编辑页事件类型树";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param(name = "id", type = ApiParamType.LONG, xss = true, desc = "主键ID")
    })
    @Output({
    	@Param(name = "eventTypeTree", type = ApiParamType.JSONARRAY, explode = EventTypeVo[].class,desc = "事件类型架构集合")
    })
    @Description(desc = "查询解决方案编辑页事件类型树")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	JSONObject resultObj = new JSONObject();
		resultObj.put("eventTypeTree", new ArrayList<>());
		Long id = jsonObj.getLong("id");

		/**
		 * 1、找出所有父节点为ROOT的节点
		 * 2、根据solutionId找出关联的事件类型列表List<X>
		 * 2、遍历每个事件类型X，找出其所在树的顶级节点
		 * 3、查询该树下层级小于X的层级的节点，
		 * 这样就可以找出X所有父节点以及兄弟节点，并做去重
		 */

		Set<EventTypeVo> eventTypeSet = new HashSet<>();
		Set<Long> eventTypeIdSet = new HashSet<>();
		Map<Long, EventTypeVo> eventTypeMap = new HashMap<>();

		EventTypeVo eventTypeVo = new EventTypeVo();
		eventTypeVo.setParentId(EventTypeVo.ROOT_ID);
		List<EventTypeVo> topEventTypeList = eventTypeMapper.searchEventType(eventTypeVo);
		/** 获取所有顶级节点 */
		eventTypeSet.addAll(topEventTypeList);
		for(EventTypeVo vo : eventTypeSet){
			eventTypeMap.put(vo.getId(), vo);
			eventTypeIdSet.add(vo.getId());
		}

		List<EventTypeVo> eventTypes = eventSolutionMapper.getEventTypeBySolutionId(id);
		/** 获取X的所有父节点及所有父节点的兄弟节点 */
		for(EventTypeVo vo : eventTypes){
			EventTypeVo topEvent = eventTypeMapper.getTopEventTypeByLftRht(vo.getLft(), vo.getRht());
			if(topEvent != null){
				List<EventTypeVo> children = eventTypeMapper.getChildrenByLftRhtLayer(topEvent.getLft(), topEvent.getRht(), vo.getLayer());
				for(EventTypeVo child : children){
					eventTypeSet.add(child);
					eventTypeMap.put(child.getId(), child);
					eventTypeIdSet.add(child.getId());
				}
			}
		}
		/** 获取X的兄弟节点及其自身 */
		for(EventTypeVo vo : eventTypes){
			List<EventTypeVo> brotherAndSelf = eventTypeMapper.getEventTypeListByParentId(vo.getParentId());
			eventTypeSet.addAll(brotherAndSelf);
			eventTypeIdSet.addAll(brotherAndSelf.stream().map(EventTypeVo::getId).collect(Collectors.toList()));
			for(EventTypeVo bs : brotherAndSelf){
				eventTypeMap.put(bs.getId(), bs);
			}
		}
		List<Long> eventTypeIdList = eventTypeIdSet.stream().collect(Collectors.toList());

		if(CollectionUtils.isNotEmpty(eventTypeSet) && CollectionUtils.isNotEmpty(eventTypeIdList)){
			EventTypeVo rootEventType = new EventTypeVo();
			rootEventType.setId(EventTypeVo.ROOT_ID);
			rootEventType.setName("root");
			rootEventType.setParentId(EventTypeVo.ROOT_PARENTID);
			eventTypeMap.put(EventTypeVo.ROOT_ID, rootEventType);
			List<EventTypeVo> eventTypeSolutionCountAndChildCountList = eventTypeMapper.getEventTypeSolutionCountAndChildCountListByIdList(eventTypeIdList);
			Map<Long, EventTypeVo> eventTypeSolutionCountAndChildCountMap = new HashMap<>();
			for(EventTypeVo eventType : eventTypeSolutionCountAndChildCountList) {
				eventTypeSolutionCountAndChildCountMap.put(eventType.getId(), eventType);
			}
			for(EventTypeVo eventType : eventTypeSet) {
				EventTypeVo parentEventType = eventTypeMap.get(eventType.getParentId());
				for(EventTypeVo vo : eventTypeSet){
					if(parentEventType.getId().equals(vo.getId())){
						parentEventType = vo;
						break;
					}
				}
				if(parentEventType != null) {
					eventType.setParent(parentEventType);
				}
				EventTypeVo eventTypeSolutionCountAndChildCount = eventTypeSolutionCountAndChildCountMap.get(eventType.getId());
				if(eventTypeSolutionCountAndChildCount != null) {
					eventType.setChildCount(eventTypeSolutionCountAndChildCount.getChildCount());
				}
			}
			resultObj.put("eventTypeTree",rootEventType.getChildren());
		}
		return resultObj;
    }
}
