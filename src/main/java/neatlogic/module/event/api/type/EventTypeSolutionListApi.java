package neatlogic.module.event.api.type;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.event.dao.mapper.EventTypeMapper;
import neatlogic.framework.event.dto.EventSolutionVo;
import neatlogic.framework.event.dto.EventTypeVo;
import neatlogic.framework.event.exception.core.EventTypeNotFoundException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeSolutionListApi extends PrivateApiComponentBase {

    @Resource
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/solution/list";
    }

    @Override
    public String getName() {
        return "获取事件类型关联的解决方案";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID", isRequired = true),
            @Param(name = "isActive", type = ApiParamType.ENUM, desc = "解决方案是否启用", rule = "0,1"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
    })
    @Output({
            @Param(name = "currentPage", type = ApiParamType.INTEGER, isRequired = true, desc = "当前页码"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, isRequired = true, desc = "页大小"),
            @Param(name = "pageCount", type = ApiParamType.INTEGER, isRequired = true, desc = "总页数"),
            @Param(name = "rowNum", type = ApiParamType.INTEGER, isRequired = true, desc = "总行数"),
            @Param(name = "solutionList", explode = EventSolutionVo[].class, desc = "关联的解决方案列表")
    })
    @Description(desc = "获取事件类型关联的解决方案")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject resultObj = new JSONObject();
        EventTypeVo eventTypeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<EventTypeVo>() {
        });
        Integer isActive = jsonObj.getInteger("isActive");
        EventTypeVo type = eventTypeMapper.getEventTypeById(eventTypeVo.getId());
        if (type == null) {
            throw new EventTypeNotFoundException(eventTypeVo.getId());
        }
        eventTypeVo.setLft(type.getLft());
        eventTypeVo.setRht(type.getRht());
        if (eventTypeVo.getNeedPage()) {
            int rowNum = eventTypeMapper.getSolutionCountByLtfRht(eventTypeVo.getLft(), eventTypeVo.getRht(), isActive);
            int pageCount = PageUtil.getPageCount(rowNum, eventTypeVo.getPageSize());
            int currentPage = eventTypeVo.getCurrentPage();
            resultObj.put("rowNum", rowNum);
            resultObj.put("pageCount", pageCount);
            resultObj.put("currentPage", currentPage);
            resultObj.put("pageSize", eventTypeVo.getPageSize());
        }
        List<EventSolutionVo> solutionList = eventTypeMapper.getSolutionListByLftRht(eventTypeVo, isActive);
        resultObj.put("solutionList", solutionList);
        return resultObj;
    }
}
