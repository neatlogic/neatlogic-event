/*Copyright (C) 2024  深圳极向量科技有限公司 All Rights Reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.*/

package neatlogic.module.event.api.type;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.event.dto.EventTypeVo;
import neatlogic.framework.event.exception.core.EventTypeNameRepeatException;
import neatlogic.framework.event.exception.core.EventTypeNotFoundException;
import neatlogic.framework.exception.type.ParamNotExistsException;
import neatlogic.framework.lrcode.LRCodeManager;
import neatlogic.framework.restful.annotation.*;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.IValid;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.util.RegexUtils;
import neatlogic.module.event.auth.label.EVENT_TYPE_MODIFY;
import neatlogic.module.event.dao.mapper.EventTypeMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;


@AuthAction(action = EVENT_TYPE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.CREATE)
public class EventTypeSaveApi extends PrivateApiComponentBase {

    @Resource
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/save";
    }

    @Override
    public String getName() {
        return "nmeat.eventtypesaveapi.getname";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "common.id"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, desc = "common.name", isRequired = true, xss = true),
            @Param(name = "parentId", type = ApiParamType.LONG, desc = "common.parentid"),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "nmeat.eventtypesaveapi.input.param.desc.authoritylist")
    })
    @Output({@Param(name = "eventTypeId", type = ApiParamType.LONG, desc = "term.event.eventtypeid")})
    @Description(desc = "nmeat.eventtypesaveapi.getname")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        Long id = jsonObj.getLong("id");
        EventTypeVo eventType = new EventTypeVo();
        eventType.setName(jsonObj.getString("name"));
        Integer solutionCount = null;
        if (id != null) {
            EventTypeVo typeVo = eventTypeMapper.getEventTypeById(id);
            if (typeVo == null) {
                throw new EventTypeNotFoundException(id);
            }
            Long parentId = typeVo.getParentId();
            typeVo.setName(eventType.getName());
            EventTypeVo parent = eventTypeMapper.getEventTypeById(parentId);
            checkNameIsRepeat(typeVo, parent, parentId, typeVo.getLayer());
            eventType.setId(id);
            eventTypeMapper.updateEventTypeNameById(eventType);
        } else {
            Integer parentLayer = 0;
            Long parentId = jsonObj.getLong("parentId");
            EventTypeVo parent = null;
            if (parentId == null) {
                parentId = EventTypeVo.ROOT_ID;
            } else if (!EventTypeVo.ROOT_ID.equals(parentId)) {
                parent = eventTypeMapper.getEventTypeById(parentId);
                if (parent == null) {
                    throw new EventTypeNotFoundException(parentId);
                }
                parentLayer = parent.getLayer();
            }
            checkNameIsRepeat(eventType, parent, parentId, parentLayer + 1);
            //更新插入位置右边的左右编码值
            int lft = LRCodeManager.beforeAddTreeNode("event_type", "id", "parent_id", parentId);
            eventType.setParentId(parentId);
            eventType.setLft(lft);
            eventType.setRht(lft + 1);
            //计算层级
//			int layer = eventTypeMapper.calculateLayer(eventType.getLft(), eventType.getRht());
            eventType.setLayer(parentLayer + 1);
            eventTypeMapper.insertEventType(eventType);
            /* 查询关联的解决方案数量，确保页面回显的数据正确 */
            EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(eventType.getLft(), eventType.getRht());
            solutionCount = count.getSolutionCount();

            /* 保存授权信息 */
            JSONArray authorityArray = jsonObj.getJSONArray("authorityList");
            if (CollectionUtils.isNotEmpty(authorityArray)) {
                List<String> authorityList = authorityArray.toJavaList(String.class);
                eventType.setAuthorityList(authorityList);
                List<AuthorityVo> authorityVoList = eventType.getAuthorityVoList();
                if (CollectionUtils.isNotEmpty(authorityVoList)) {
                    for (AuthorityVo authorityVo : authorityVoList) {
                        eventTypeMapper.insertEventTypeAuthority(authorityVo, eventType.getId());
                    }
                }
            }
        }

        JSONObject returnObj = new JSONObject();
        returnObj.put("eventTypeId", eventType.getId());
        returnObj.put("solutionCount", solutionCount);
        return returnObj;
    }

    /**
     * 校验名称是否重复，规则：同一层级中不能出现重名事件类型
     *
     * @param target   待校验的事件类型
     * @param parent   待校验类型的父类型
     * @param parentId 父类型id
     * @param layer    层级
     */
    private void checkNameIsRepeat(EventTypeVo target, EventTypeVo parent, Long parentId, Integer layer) {
        EventTypeVo searchVo = new EventTypeVo();
        if (!Objects.equals(parentId, EventTypeVo.ROOT_ID)) {
            searchVo.setId(target.getId());
            searchVo.setLft(parent.getLft());
            searchVo.setRht(parent.getRht());
            searchVo.setLayer(layer);
            searchVo.setName(target.getName());
            if (eventTypeMapper.checkEventTypeNameIsRepeatByLRAndLayer(searchVo) > 0) {
                throw new EventTypeNameRepeatException();
            }
        } else {
            searchVo.setId(target.getId());
            searchVo.setName(target.getName());
            searchVo.setParentId(EventTypeVo.ROOT_ID);
            if (eventTypeMapper.checkEventTypeNameIsRepeatByParentId(searchVo) > 0) {
                throw new EventTypeNameRepeatException();
            }
        }
    }

    public IValid name() {
        return jsonObj -> {
            String name = jsonObj.getString("name");
            if (StringUtils.isBlank(name)) {
                throw new ParamNotExistsException("name");
            }
            Long parentId = jsonObj.getLong("parentId");
            if (parentId == null) {
                parentId = EventTypeVo.ROOT_ID;
            } else if (!EventTypeVo.ROOT_ID.equals(parentId)) {
                EventTypeVo parent = eventTypeMapper.getEventTypeById(parentId);
                if (parent == null) {
                    throw new EventTypeNotFoundException(parentId);
                }
            }
            EventTypeVo searchVo = new EventTypeVo();
            searchVo.setId(jsonObj.getLong("id"));
            searchVo.setName(name);
            searchVo.setParentId(parentId);
            if (eventTypeMapper.checkEventTypeNameIsRepeatByParentId(searchVo) > 0) {
                return new FieldValidResultVo(new EventTypeNameRepeatException());
            }
            return new FieldValidResultVo();
        };
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
