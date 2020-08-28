package codedriver.module.event.api.type;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.reminder.core.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventTypeVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeChildrenGetApi extends PrivateApiComponentBase {

    @Autowired
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/children/get";
    }

    @Override
    public String getName() {
        return "查询指定事件类型下的所有子类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({
    	@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "主键ID")
    })
    @Output({
    	@Param(name = "children", type = ApiParamType.JSONARRAY, explode = EventTypeVo[].class,desc = "事件类型架构集合")
    })
    @Description(desc = "查询指定事件类型下的所有子类型")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
    	JSONObject resultObj = new JSONObject();
		List<EventTypeVo> eventTypeList = null;
		Long id = jsonObj.getLong("id");
		EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(id);
		if(eventTypeVo == null) {
			throw new EventTypeNotFoundException(id);
		}

		eventTypeList = eventTypeMapper.getChildrenByLeftRightCode(eventTypeVo.getLft(), eventTypeVo.getRht());
		resultObj.put("children",eventTypeList);
    	return resultObj;
    }
}
