package neatlogic.module.event.api.solution;

import neatlogic.framework.asynchronization.threadlocal.UserContext;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Output;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.event.auth.label.EVENT_SOLUTION_MODIFY;
import neatlogic.module.event.dao.mapper.EventSolutionMapper;
import neatlogic.framework.event.dto.EventSolutionVo;
import neatlogic.framework.event.exception.core.EventSolutionNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@AuthAction(action = EVENT_SOLUTION_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class EventSolutionStatusUpdateApi extends PrivateApiComponentBase {

    @Resource
    private EventSolutionMapper eventSolutionMapper;

    @Override
    public String getToken() {
        return "event/solution/status/update";
    }

    @Override
    public String getName() {
        return "修改解决方案激活状态";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "id", type = ApiParamType.LONG, isRequired = true, desc = "解决方案ID"),
            @Param(name = "isActive", type = ApiParamType.INTEGER, isRequired = true, desc = "是否激活")
    })
    @Output({})
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        Integer isActive = jsonObj.getInteger("isActive");

        EventSolutionVo eventSolutionVo = new EventSolutionVo();
        eventSolutionVo.setId(id);
        eventSolutionVo.setIsActive(isActive);
        eventSolutionVo.setLcu(UserContext.get().getUserUuid(true));

        if (eventSolutionMapper.checkSolutionExistsById(id) == null) {
            throw new EventSolutionNotFoundException(id);
        }
        eventSolutionMapper.updateSolutionStatus(eventSolutionVo);
        return null;
    }
}
