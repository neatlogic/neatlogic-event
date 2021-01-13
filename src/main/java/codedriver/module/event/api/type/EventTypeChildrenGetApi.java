//package codedriver.module.event.api.type;
//
//import codedriver.framework.common.constvalue.ApiParamType;
//import codedriver.framework.process.exception.event.EventTypeNotFoundException;
//import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
//import codedriver.framework.restful.annotation.*;
//import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
//import codedriver.module.event.dao.mapper.EventTypeMapper;
//import codedriver.module.event.dto.EventTypeVo;
//import com.alibaba.fastjson.JSONObject;
//import org.apache.commons.collections4.CollectionUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@OperationType(type = OperationTypeEnum.SEARCH)
//public class EventTypeChildrenGetApi extends PrivateApiComponentBase {
//
//    @Autowired
//    private EventTypeMapper eventTypeMapper;
//
//    @Override
//    public String getToken() {
//        return "eventtype/children/get";
//    }
//
//    @Override
//    public String getName() {
//        return "查询指定事件类型下的所有子类型";
//    }
//
//    @Override
//    public String getConfig() {
//        return null;
//    }
//
//    @Input({
//    	@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "主键ID")
//    })
//    @Output({
//    	@Param(name = "children", type = ApiParamType.JSONARRAY, explode = EventTypeVo[].class,desc = "事件类型架构集合")
//    })
//    @Description(desc = "查询指定事件类型下的所有子类型")
//    @Override
//    public Object myDoService(JSONObject jsonObj) throws Exception {
//    	JSONObject resultObj = new JSONObject();
//		List<EventTypeVo> eventTypeList = null;
//        Map<Long, EventTypeVo> eventTypeMap = new HashMap<>();
//        List<Long> eventTypeIdList = new ArrayList<>();
//		Long id = jsonObj.getLong("id");
//		EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(id);
//		if(eventTypeVo == null) {
//			throw new EventTypeNotFoundException(id);
//		}
//
//		eventTypeList = eventTypeMapper.getChildrenByLeftRightCode(eventTypeVo.getLft(), eventTypeVo.getRht());
//        if(CollectionUtils.isNotEmpty(eventTypeList)){
//            for(EventTypeVo vo : eventTypeList){
//                eventTypeMap.put(vo.getId(), vo);
//                eventTypeIdList.add(vo.getId());
//            }
//            eventTypeMap.put(eventTypeVo.getId(), eventTypeVo);
//            List<EventTypeVo> childCountList = eventTypeMapper.getEventTypeSolutionCountAndChildCountListByIdList(eventTypeIdList);
//            Map<Long, EventTypeVo> childCountMap = new HashMap<>();
//            for(EventTypeVo eventType : childCountList) {
//                childCountMap.put(eventType.getId(), eventType);
//            }
//            for(EventTypeVo eventType : eventTypeList) {
//                EventTypeVo parentEventType = eventTypeMap.get(eventType.getParentId());
//                if(parentEventType != null) {
//                    eventType.setParent(parentEventType);
//                }
//                EventTypeVo childCount = childCountMap.get(eventType.getId());
//                if(childCount != null) {
//                    eventType.setChildCount(childCount.getChildCount());
//                }
//            }
//        }
//        resultObj.put("children",eventTypeVo.getChildren());
//    	return resultObj;
//    }
//}
