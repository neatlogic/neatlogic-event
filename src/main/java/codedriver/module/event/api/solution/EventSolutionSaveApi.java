package codedriver.module.event.api.solution;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.FieldValidResultVo;
import codedriver.framework.event.auth.label.EVENT_SOLUTION_MODIFY;
import codedriver.framework.event.dto.EventSolutionVo;
import codedriver.framework.event.exception.core.EventSolutionNotFoundException;
import codedriver.framework.event.exception.core.EventSolutionRepeatException;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.IValid;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.framework.util.RegexUtils;
import codedriver.module.event.dao.mapper.EventSolutionMapper;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;


@AuthAction(action = EVENT_SOLUTION_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class EventSolutionSaveApi extends PrivateApiComponentBase {

    @Resource
    private EventSolutionMapper eventSolutionMapper;

    @Override
    public String getToken() {
        return "event/solution/save";
    }

    @Override
    public String getName() {
        return "保存解决方案";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "解决方案ID"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, desc = "解决方案名称", isRequired = true, xss = true),
            @Param(name = "isActive", type = ApiParamType.INTEGER, desc = "是否激活", isRequired = true),
            @Param(name = "content", type = ApiParamType.STRING, desc = "内容", isRequired = true),
            @Param(name = "eventTypeList", type = ApiParamType.JSONARRAY, desc = "关联的事件类型ID集合", isRequired = true)
    })
    @Output({@Param(name = "solutionId", type = ApiParamType.LONG, desc = "保存的解决方案ID")})
    @Description(desc = "保存解决方案")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        Long id = jsonObj.getLong("id");
        String name = jsonObj.getString("name");
        String content = jsonObj.getString("content");
        JSONArray eventTypeList = jsonObj.getJSONArray("eventTypeList");
        List<Long> eventTypeIds = eventTypeList.toJavaList(Long.class);
        EventSolutionVo eventSolutionVo = new EventSolutionVo();

        eventSolutionVo.setName(name);
        if (id == null) {
            if (eventSolutionMapper.checkSolutionNameIsRepeat(eventSolutionVo) > 0) {
                throw new EventSolutionRepeatException(name);
            }
            eventSolutionVo.setContent(content);
            eventSolutionVo.setIsActive(1);
            eventSolutionVo.setFcu(UserContext.get().getUserUuid());
            eventSolutionMapper.insertSolution(eventSolutionVo);
            for (Long eventTypeId : eventTypeIds) {
                eventSolutionMapper.insertEventTypeSolution(eventTypeId, eventSolutionVo.getId());
            }
        } else {
            eventSolutionVo.setId(id);
            if (eventSolutionMapper.checkSolutionExistsById(id) == null) {
                throw new EventSolutionNotFoundException(id);
            }
            if (eventSolutionMapper.checkSolutionNameIsRepeat(eventSolutionVo) > 0) {
                throw new EventSolutionRepeatException(name);
            }
            Integer isActive = jsonObj.getInteger("isActive");
            eventSolutionVo.setIsActive(isActive);
            eventSolutionVo.setContent(content);
            eventSolutionVo.setLcu(UserContext.get().getUserUuid());
            eventSolutionVo.setLcd(new Date());
            eventSolutionMapper.updateSolutionById(eventSolutionVo);
            /** 关联事件类型 */
            eventSolutionMapper.deleteEventTypeSolution(id);
            for (Long eventTypeId : eventTypeIds) {
                eventSolutionMapper.insertEventTypeSolution(eventTypeId, id);
            }

        }
        returnObj.put("solutionId", eventSolutionVo.getId());
        return returnObj;
    }

    public IValid name() {
        return value -> {
            EventSolutionVo eventSolutionVo = JSON.toJavaObject(value, EventSolutionVo.class);
            if (eventSolutionMapper.checkSolutionNameIsRepeat(eventSolutionVo) > 0) {
                return new FieldValidResultVo(new EventSolutionRepeatException(eventSolutionVo.getName()));
            }
            return new FieldValidResultVo();
        };
    }
}
