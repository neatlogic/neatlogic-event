package codedriver.module.event.api.type;

import java.util.List;

import codedriver.framework.lrcode.LRCodeManager;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.dto.AuthorityVo;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.restful.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.Description;
import codedriver.framework.restful.annotation.Input;
import codedriver.framework.restful.annotation.OperationType;
import codedriver.framework.restful.annotation.Output;
import codedriver.framework.restful.annotation.Param;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.auth.label.EVENT_TYPE_MODIFY;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventTypeVo;


@AuthAction(action = EVENT_TYPE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class EventTypeSaveApi extends PrivateApiComponentBase{

	@Autowired
	private EventTypeMapper eventTypeMapper;

	@Override
	public String getToken() {
		return "eventtype/save";
	}

	@Override
	public String getName() {
		return "保存事件类型信息";
	}

	@Override
	public String getConfig() {
		return null;
	}


	@Input({
			@Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID"),
			@Param(name = "name", type = ApiParamType.REGEX, rule = "^[A-Za-z_\\d\\u4e00-\\u9fa5]+$", desc = "事件类型名称",isRequired=true, xss=true),
			@Param(name = "parentId", type = ApiParamType.LONG, desc = "父类型id"),
			@Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
	})
	@Output({@Param(name = "eventTypeId", type = ApiParamType.LONG, desc = "保存的事件类型ID")})
	@Description(desc = "保存事件类型信息")
	@Override
	public Object myDoService(JSONObject jsonObj) throws Exception {
		Long id = jsonObj.getLong("id");
		EventTypeVo eventType = new EventTypeVo();
		eventType.setName(jsonObj.getString("name"));
		Integer solutionCount = null;
		if(id != null){
			if(eventTypeMapper.checkEventTypeIsExists(id) == 0){
				throw new EventTypeNotFoundException(id);
			}
			eventType.setId(id);
			eventTypeMapper.updateEventTypeNameById(eventType);
		}else{
			Integer parentLayer = 0;
			Long parentId = jsonObj.getLong("parentId");
			if (parentId == null){
				parentId = EventTypeVo.ROOT_ID;
			}else if(!EventTypeVo.ROOT_ID.equals(parentId)){
				EventTypeVo parentEventType = eventTypeMapper.getEventTypeById(parentId);
				if(parentEventType == null) {
					throw new EventTypeNotFoundException(parentId);
				}
				parentLayer = parentEventType.getLayer();
			}
			//更新插入位置右边的左右编码值
			int lft = LRCodeManager.beforeAddTreeNode("event_type", "id", "parent_id", parentId);
			eventType.setParentId(parentId);
			eventType.setLft(lft);
			eventType.setRht(lft + 1);
			//计算层级
//			int layer = eventTypeMapper.calculateLayer(eventType.getLft(), eventType.getRht());
			eventType.setLayer(parentLayer + 1);
			eventTypeMapper.insertEventType(eventType);
			/** 查询关联的解决方案数量，确保页面回显的数据正确 */
			EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(eventType.getLft(), eventType.getRht());
			solutionCount = count.getSolutionCount();

			/** 保存授权信息 */
			JSONArray authorityArray = jsonObj.getJSONArray("authorityList");
			if(CollectionUtils.isNotEmpty(authorityArray)){
				List<String> authorityList = authorityArray.toJavaList(String.class);
				eventType.setAuthorityList(authorityList);
				List<AuthorityVo> authorityVoList = eventType.getAuthorityVoList();
				if(CollectionUtils.isNotEmpty(authorityVoList)){
					for(AuthorityVo authorityVo : authorityVoList) {
						eventTypeMapper.insertEventTypeAuthority(authorityVo,eventType.getId());
					}
				}
			}
		}

		JSONObject returnObj = new JSONObject();
		returnObj.put("eventTypeId",eventType.getId());
		returnObj.put("solutionCount",solutionCount);
		return returnObj;
	}

//	private Object backup(JSONObject jsonObj) throws Exception {
//		JSONObject returnObj = new JSONObject();
//		Long id = jsonObj.getLong("id");
//		EventTypeVo eventType = new EventTypeVo();
//		eventType.setName(jsonObj.getString("name"));
//		Integer solutionCount = null;
//		if(id != null){
//			if(eventTypeMapper.checkEventTypeIsExists(id) == 0){
//				throw new EventTypeNotFoundException(id);
//			}
//			eventType.setId(id);
//			eventTypeMapper.updateEventTypeNameById(eventType);
//		}else{
//			eventTypeMapper.getEventTypeCountOnLock();
//			if(eventTypeMapper.checkLeftRightCodeIsWrong() > 0) {
//				eventTypeService.rebuildLeftRightCode();
//			}
//			Long parentId = jsonObj.getLong("parentId");
//			if (parentId == null){
//				parentId = EventTypeVo.ROOT_ID;
//			}
//			EventTypeVo parentEventType;
//			if(EventTypeVo.ROOT_ID.equals(parentId)){
//				parentEventType = eventTypeService.buildRootEventType();
//			}else{
//				parentEventType = eventTypeMapper.getEventTypeById(parentId);
//				if(parentEventType == null) {
//					throw new EventTypeNotFoundException(parentId);
//				}
//			}
//			eventType.setParentId(parentId);
//			eventType.setLft(parentEventType.getRht());
//			eventType.setRht(eventType.getLft() + 1);
//			//更新插入位置右边的左右编码值
//			eventTypeMapper.batchUpdateEventTypeLeftCode(eventType.getLft(), 2);
//			eventTypeMapper.batchUpdateEventTypeRightCode(eventType.getLft(), 2);
//
//			//计算层级
////			int layer = eventTypeMapper.calculateLayer(eventType.getLft(), eventType.getRht());
//			eventType.setLayer(parentEventType.getLayer() + 1);
//			eventTypeMapper.insertEventType(eventType);
//			/** 查询关联的解决方案数量，确保页面回显的数据正确 */
//			EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(eventType.getLft(), eventType.getRht());
//			solutionCount = count.getSolutionCount();
//
//			/** 保存授权信息 */
//			JSONArray authorityArray = jsonObj.getJSONArray("authorityList");
//			if(CollectionUtils.isNotEmpty(authorityArray)){
//				List<String> authorityList = authorityArray.toJavaList(String.class);
//				eventType.setAuthorityList(authorityList);
//				List<AuthorityVo> authorityVoList = eventType.getAuthorityVoList();
//				if(CollectionUtils.isNotEmpty(authorityVoList)){
//					for(AuthorityVo authorityVo : authorityVoList) {
//						eventTypeMapper.insertEventTypeAuthority(authorityVo,eventType.getId());
//					}
//				}
//			}
//		}
//		returnObj.put("eventTypeId",eventType.getId());
//		returnObj.put("solutionCount",solutionCount);
//		return returnObj;
//	}
}
