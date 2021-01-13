package codedriver.module.event.api.type;

import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.common.util.PageUtil;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventSolutionVo;
import codedriver.module.event.dto.EventTypeVo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@OperationType(type = OperationTypeEnum.SEARCH)
public class EventTypeSolutionListApi extends PrivateApiComponentBase{

	@Autowired
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
			@Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID",isRequired = true),
			@Param(name = "isActive", type = ApiParamType.ENUM, desc = "解决方案是否启用",rule = "0,1"),
			@Param(name = "needPage", type = ApiParamType.BOOLEAN, desc = "是否需要分页，默认true"),
			@Param(name = "pageSize", type = ApiParamType.INTEGER, desc = "每页条目"),
			@Param(name = "currentPage", type = ApiParamType.INTEGER, desc = "当前页")
	})
    @Output({
			@Param(name="currentPage",type=ApiParamType.INTEGER,isRequired=true,desc="当前页码"),
			@Param(name="pageSize",type=ApiParamType.INTEGER,isRequired=true,desc="页大小"),
			@Param(name="pageCount",type=ApiParamType.INTEGER,isRequired=true,desc="总页数"),
			@Param(name="rowNum",type=ApiParamType.INTEGER,isRequired=true,desc="总行数"),
			@Param(name="solutionList", explode = EventSolutionVo[].class, desc = "关联的解决方案列表")
    })
	@Description(desc = "获取事件类型关联的解决方案")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		JSONObject resultObj = new JSONObject();
		EventTypeVo eventTypeVo = JSON.parseObject(jsonObj.toJSONString(), new TypeReference<EventTypeVo>() {});
		Integer isActive = jsonObj.getInteger("isActive");
		EventTypeVo type = eventTypeMapper.getEventTypeById(eventTypeVo.getId());
		if (type == null) {
			throw new EventTypeNotFoundException(eventTypeVo.getId());
		}
		eventTypeVo.setLft(type.getLft());
		eventTypeVo.setRht(type.getRht());
		if(eventTypeVo.getNeedPage()){
			int rowNum = eventTypeMapper.getSolutionCountByLtfRht(eventTypeVo.getLft(),eventTypeVo.getRht(),isActive);
			int pageCount = PageUtil.getPageCount(rowNum, eventTypeVo.getPageSize());
			int currentPage = eventTypeVo.getCurrentPage();
			resultObj.put("rowNum", rowNum);
			resultObj.put("pageCount", pageCount);
			resultObj.put("currentPage", currentPage);
			resultObj.put("pageSize", eventTypeVo.getPageSize());
		}
		List<EventSolutionVo> solutionList = eventTypeMapper.getSolutionListByLftRht(eventTypeVo,isActive);
		resultObj.put("solutionList",solutionList);
		return resultObj;
	}
}
