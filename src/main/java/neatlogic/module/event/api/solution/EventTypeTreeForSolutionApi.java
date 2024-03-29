package neatlogic.module.event.api.solution;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.event.dao.mapper.EventSolutionMapper;
import neatlogic.module.event.dao.mapper.EventTypeMapper;
import neatlogic.framework.event.dto.EventTypeVo;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeTreeForSolutionApi extends PrivateApiComponentBase {

    @Resource
    private EventTypeMapper eventTypeMapper;

    @Resource
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
            @Param(name = "id", type = ApiParamType.LONG, desc = "主键ID")
    })
    @Output({
            @Param(name = "eventTypeTree", type = ApiParamType.JSONARRAY, explode = EventTypeVo[].class, desc = "事件类型架构集合")
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
//		Map<Long, EventTypeVo> eventTypeMap = new HashMap<>();

        EventTypeVo eventTypeVo = new EventTypeVo();
        eventTypeVo.setParentId(EventTypeVo.ROOT_ID);
        List<EventTypeVo> topEventTypeList = eventTypeMapper.searchEventType(eventTypeVo);
        /** 获取所有顶级节点 */
        eventTypeSet.addAll(topEventTypeList);
        if (CollectionUtils.isNotEmpty(eventTypeSet)) {
            for (EventTypeVo vo : eventTypeSet) {
//				eventTypeMap.put(vo.getId(), vo);
                eventTypeIdSet.add(vo.getId());
            }
        }

        List<EventTypeVo> eventTypes = eventSolutionMapper.getEventTypeBySolutionId(id);
        if (CollectionUtils.isNotEmpty(eventTypes)) {
            /** 获取X的所有父节点及所有父节点的兄弟节点 */
            for (EventTypeVo vo : eventTypes) {
                EventTypeVo topEvent = eventTypeMapper.getTopEventTypeByLftRht(vo.getLft(), vo.getRht());
                if (topEvent != null) {
                    List<EventTypeVo> children = eventTypeMapper.getChildrenByLftRhtLayer(topEvent.getLft(), topEvent.getRht(), vo.getLayer());
                    for (EventTypeVo child : children) {
                        eventTypeSet.add(child);
//						eventTypeMap.put(child.getId(), child);
                        eventTypeIdSet.add(child.getId());
                    }
                }
            }
            /** 获取X的兄弟节点及其自身 */
            for (EventTypeVo vo : eventTypes) {
                List<EventTypeVo> brotherAndSelf = eventTypeMapper.getEventTypeListByParentId(vo.getParentId());
                eventTypeSet.addAll(brotherAndSelf);
                eventTypeIdSet.addAll(brotherAndSelf.stream().map(EventTypeVo::getId).collect(Collectors.toList()));
//				for(EventTypeVo bs : brotherAndSelf){
//					eventTypeMap.put(bs.getId(), bs);
//				}
            }
        }

        List<Long> eventTypeIdList = eventTypeIdSet.stream().collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(eventTypeSet) && CollectionUtils.isNotEmpty(eventTypeIdList)) {
            EventTypeVo rootEventType = new EventTypeVo();
            rootEventType.setId(EventTypeVo.ROOT_ID);
            rootEventType.setName("root");
            rootEventType.setParentId(EventTypeVo.ROOT_PARENTID);
//			eventTypeMap.put(EventTypeVo.ROOT_ID, rootEventType);
            eventTypeSet.add(rootEventType);
            List<EventTypeVo> eventTypeChildCountList = eventTypeMapper.getEventTypeChildCountListByIdList(eventTypeIdList);
            Map<Long, EventTypeVo> eventTypeChildCountMap = new HashMap<>();
            for (EventTypeVo eventType : eventTypeChildCountList) {
                eventTypeChildCountMap.put(eventType.getId(), eventType);
            }
            for (EventTypeVo eventType : eventTypeSet) {
                EventTypeVo parentEventType = null;
                for (EventTypeVo vo : eventTypeSet) {
                    if (eventType.getParentId().equals(vo.getId())) {
                        parentEventType = vo;
                        break;
                    }
                }
                if (parentEventType != null) {
                    eventType.setParent(parentEventType);
                }
                EventTypeVo eventTypeChildCount = eventTypeChildCountMap.get(eventType.getId());
                if (eventTypeChildCount != null) {
                    eventType.setChildCount(eventTypeChildCount.getChildCount());
                }
            }
            resultObj.put("eventTypeTree", rootEventType.getChildren());
        }
        return resultObj;
    }
}
