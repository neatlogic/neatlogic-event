package neatlogic.module.event.api.solution;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.common.util.PageUtil;
import neatlogic.framework.process.auth.PROCESS_BASE;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.module.event.dao.mapper.EventSolutionMapper;
import neatlogic.framework.event.dto.EventSolutionVo;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventSolutionSearchApi extends PrivateApiComponentBase {

    @Resource
    private EventSolutionMapper eventSolutionMapper;

    @Override
    public String getToken() {
        return "event/solution/search";
    }

    @Override
    public String getName() {
        return "查询解决方案";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "keyword", type = ApiParamType.STRING, desc = "关键字", xss = true),
            @Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页"),
            @Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页数据条目"),
            @Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true")
    })
    @Output({@Param(name = "solutionList",
            type = ApiParamType.JSONARRAY,
            explode = EventSolutionVo[].class,
            desc = "解决方案列表")})
    @Description(desc = "查询解决方案")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        EventSolutionVo eventSolutionVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<EventSolutionVo>() {
        });
        JSONObject returnObj = new JSONObject();
        if (eventSolutionVo.getNeedPage()) {
            int rowNum = eventSolutionMapper.searchSolutionCount(eventSolutionVo);
            returnObj.put("pageSize", eventSolutionVo.getPageSize());
            returnObj.put("currentPage", eventSolutionVo.getCurrentPage());
            returnObj.put("rowNum", rowNum);
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, eventSolutionVo.getPageSize()));
        }
        List<EventSolutionVo> solutionList = eventSolutionMapper.searchSolution(eventSolutionVo);
        returnObj.put("solutionList", solutionList);
        return returnObj;
    }
}
