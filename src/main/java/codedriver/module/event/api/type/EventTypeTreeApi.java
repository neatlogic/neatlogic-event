package codedriver.module.event.api.type;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.dto.BasePageVo;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.auth.PROCESS_BASE;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventTypeVo;
import codedriver.module.event.exception.core.EventTypeNotFoundException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AuthAction(action = PROCESS_BASE.class)
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeTreeApi extends PrivateApiComponentBase {

    @Resource
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/tree";
    }

    @Override
    public String getName() {
        return "获取事件类型架构树";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({@Param(name = "parentId", desc = "parentId，这里指父级id", type = ApiParamType.LONG),
            @Param(name = "currentPage", desc = "当前页", type = ApiParamType.INTEGER),
            @Param(name = "needPage", desc = "是否分页", type = ApiParamType.BOOLEAN),
            @Param(name = "pageSize", desc = "每页最大数", type = ApiParamType.INTEGER)
    })
    @Output({
            @Param(name = "tbodyList", explode = EventTypeVo[].class, desc = "事件类型架构集合"),
            @Param(explode = BasePageVo.class)
    })
    @Description(desc = "获取事件类型架构树")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        JSONObject returnObj = new JSONObject();
        EventTypeVo eventTypeVo = new EventTypeVo();
        Boolean needPage = jsonObj.getBoolean("needPage");
        if (needPage != null) {
            eventTypeVo.setNeedPage(needPage);
        }
        eventTypeVo.setCurrentPage(jsonObj.getInteger("currentPage"));
        eventTypeVo.setPageSize(jsonObj.getInteger("pageSize"));
        Long parentId = jsonObj.getLong("parentId");
        if (parentId != null) {
            if (eventTypeMapper.checkEventTypeIsExists(parentId) == 0) {
                throw new EventTypeNotFoundException(parentId);
            }
        } else {
            parentId = EventTypeVo.ROOT_ID;
        }
        eventTypeVo.setParentId(parentId);
        if (eventTypeVo.getNeedPage()) {
            int rowNum = eventTypeMapper.searchEventTypeCount(eventTypeVo);
            returnObj.put("currentPage", eventTypeVo.getCurrentPage());
            returnObj.put("pageCount", PageUtil.getPageCount(rowNum, eventTypeVo.getPageSize()));
            returnObj.put("pageSize", eventTypeVo.getPageSize());
            returnObj.put("rowNum", rowNum);
        }
        List<EventTypeVo> tbodyList = eventTypeMapper.searchEventType(eventTypeVo);

        /** 查询子类和关联的解决方案数量 */
        if (CollectionUtils.isNotEmpty(tbodyList)) {

            Map<Long, EventTypeVo> eventTypeSolutionCountMap = new HashMap<>();
            for (EventTypeVo vo : tbodyList) {
                EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(vo.getLft(), vo.getRht());
                count.setId(vo.getId());
                eventTypeSolutionCountMap.put(vo.getId(), count);
            }
            List<Long> eventTypeIdList = tbodyList.stream().map(EventTypeVo::getId).collect(Collectors.toList());
            List<EventTypeVo> eventTypeChildCountList = eventTypeMapper.getEventTypeChildCountListByIdList(eventTypeIdList);
            Map<Long, EventTypeVo> eventTypeChildCountMap = new HashMap<>();
            for (EventTypeVo eventType : eventTypeChildCountList) {
                eventTypeChildCountMap.put(eventType.getId(), eventType);
            }
            for (EventTypeVo eventType : tbodyList) {
                EventTypeVo eventTypeChildCount = eventTypeChildCountMap.get(eventType.getId());
                EventTypeVo eventTypeSolutionCount = eventTypeSolutionCountMap.get(eventType.getId());
                if (eventTypeChildCount != null) {
                    eventType.setChildCount(eventTypeChildCount.getChildCount());
                }
                if (eventTypeSolutionCount != null) {
                    eventType.setSolutionCount(eventTypeSolutionCount.getSolutionCount());
                }
            }
        }
        returnObj.put("tbodyList", tbodyList);
        return returnObj;
    }
}
