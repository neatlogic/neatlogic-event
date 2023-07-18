package neatlogic.module.event.api.solution;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.event.dao.mapper.EventSolutionMapper;
import neatlogic.framework.event.dto.EventSolutionVo;
import neatlogic.framework.event.exception.core.EventSolutionNotFoundEditTargetException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventSolutionGetApi extends PrivateApiComponentBase {

    @Resource
    private EventSolutionMapper eventSolutionMapper;

    @Override
    public String getToken() {
        return "event/solution/get";
    }

    @Override
    public String getName() {
        return "nmeas.eventsolutiongetapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id", isRequired = true)
    })
    @Output({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.STRING, desc = "common.name"),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "common.isactive"),
            @Param(name = "content", type = ApiParamType.STRING, desc = "common.content"),
            @Param(name = "fcu", type = ApiParamType.STRING, desc = "common.createuser"),
            @Param(name = "lcu", type = ApiParamType.STRING, desc = "common.editor"),
            @Param(name = "fcd", desc = "common.createdate"),
            @Param(name = "lcd", desc = "common.editdate"),
            @Param(name = "eventTypeList", type = ApiParamType.JSONARRAY, desc = "term.event.eventtypelist"),
    })
    @Description(desc = "nmeas.eventsolutiongetapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject result = new JSONObject();
        Long id = jsonObj.getLong("id");
        EventSolutionVo solution = eventSolutionMapper.getSolutionById(id);
        if (solution == null) {
            throw new EventSolutionNotFoundEditTargetException(id);
        }
        result.put("solution", solution);
        return result;
    }
}
