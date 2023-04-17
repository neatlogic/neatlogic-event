/*
 * Copyright(c) 2023 NeatLogic Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neatlogic.module.event.api.type;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.dto.FieldValidResultVo;
import neatlogic.framework.event.auth.label.EVENT_TYPE_MODIFY;
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
import neatlogic.module.event.dao.mapper.EventTypeMapper;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
        return "保存事件类型信息";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID"),
            @Param(name = "name", type = ApiParamType.REGEX, rule = RegexUtils.NAME, desc = "事件类型名称", isRequired = true, xss = true),
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
