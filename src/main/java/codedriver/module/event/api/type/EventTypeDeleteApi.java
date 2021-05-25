package codedriver.module.event.api.type;

import codedriver.framework.lrcode.LRCodeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.auth.label.EVENT_TYPE_MODIFY;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventTypeVo;

import java.util.List;

@AuthAction(action = EVENT_TYPE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.DELETE)
public class EventTypeDeleteApi extends PrivateApiComponentBase {

	@Autowired
	private EventTypeMapper eventTypeMapper;

	@Override
	public String getToken() {
		return "eventtype/delete";
	}

	@Override
	public String getName() {
		return "删除事件类型";
	}

	@Override
	public String getConfig() {
		return null;
	}

	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "事件类型id", isRequired = true)
	})
	@Description(desc = "删除事件类型")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		EventTypeVo eventType = eventTypeMapper.getEventTypeById(id);
		if (eventType == null) {
			throw new EventTypeNotFoundException(id);
		}
		List<Long> idList = eventTypeMapper.getChildrenIdListByLeftRightCode(eventType.getLft(), eventType.getRht());
		LRCodeManager.beforeDeleteTreeNode("event_type", "id", "parent_id", id);
		idList.add(id);
		eventTypeMapper.deleteEventTypeByIdList(idList);
		eventTypeMapper.deleteEventTypeAuthorityByEventTypeIdList(idList);
		eventTypeMapper.deleteEventTypeSolutionByEventTypeIdList(idList);
		return null;
	}

//	private Object backup(JSONObject jsonObj){
//		eventTypeMapper.getEventTypeCountOnLock();
//		if(eventTypeMapper.checkLeftRightCodeIsWrong() > 0) {
//			eventTypeService.rebuildLeftRightCode();
//		}
//		Long id = jsonObj.getLong("id");
//		EventTypeVo eventType = eventTypeMapper.getEventTypeById(id);
//		if(eventType == null) {
//			throw new EventTypeNotFoundException(id);
//		}
//
//		eventTypeMapper.deleteEventTypeByLeftRightCode(eventType.getLft(), eventType.getRht());
//		//计算被删除块右边的节点移动步长
//		int step = eventType.getRht() - eventType.getLft() + 1;
//		eventTypeMapper.batchUpdateEventTypeLeftCode(eventType.getLft(), -step);
//		eventTypeMapper.batchUpdateEventTypeRightCode(eventType.getLft(), -step);
//
//		return null;
//	}
}
