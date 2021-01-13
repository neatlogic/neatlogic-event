package codedriver.module.event.api.solution;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.exception.event.EventSolutionNotFoundException;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.auth.label.EVENT_SOLUTION_MODIFY;
import codedriver.module.event.dao.mapper.EventSolutionMapper;
import codedriver.module.event.dto.EventSolutionVo;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@AuthAction(action = EVENT_SOLUTION_MODIFY.class)
@Service
@OperationType(type = OperationTypeEnum.UPDATE)
public class EventSolutionStatusUpdateApi extends PrivateApiComponentBase {

    @Autowired
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

    @Input({ @Param( name = "id", type = ApiParamType.LONG,isRequired = true,desc = "解决方案ID"),
             @Param( name = "isActive", type = ApiParamType.INTEGER,isRequired = true,desc = "是否激活")
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

        if(eventSolutionMapper.checkSolutionExistsById(id) == null){
            throw new EventSolutionNotFoundException(id);
        }
        eventSolutionMapper.updateSolutionStatus(eventSolutionVo);
        return null;
    }
}
