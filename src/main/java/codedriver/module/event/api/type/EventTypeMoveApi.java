package codedriver.module.event.api.type;

import codedriver.framework.auth.core.AuthAction;
import codedriver.framework.common.constvalue.ApiParamType;
import codedriver.framework.process.exception.event.EventTypeMoveException;
import codedriver.framework.process.exception.event.EventTypeNotFoundException;
import codedriver.framework.restful.core.constvalue.OperationTypeEnum;
import codedriver.framework.restful.annotation.*;
import codedriver.framework.restful.core.privateapi.PrivateApiComponentBase;
import codedriver.module.event.auth.label.EVENT_TYPE_MODIFY;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.module.event.dto.EventTypeVo;
import codedriver.module.event.service.EventTypeService;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AuthAction(action = EVENT_TYPE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.UPDATE)
public class EventTypeMoveApi extends PrivateApiComponentBase {

    @Autowired
    private EventTypeService eventTypeService;

    @Autowired
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/tree/move";
    }

    @Override
    public String getName() {
        return "移动事件类型";
    }

    @Override
    public String getConfig() {
        return null;
    }

    @Input({ @Param( name = "id", type = ApiParamType.LONG, desc = "事件类型id", isRequired = true),
             @Param( name = "parentId", type = ApiParamType.LONG, desc = "父id", isRequired = true,minLength = 1),
             @Param( name = "sort", type = ApiParamType.INTEGER, desc = "sort(目标父级的位置，从0开始)", isRequired = true)})
    @Output({

    })
    @Description( desc = "移动事件类型")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {
        eventTypeMapper.getEventTypeCountOnLock();
		if(eventTypeMapper.checkLeftRightCodeIsWrong() > 0) {
            eventTypeService.rebuildLeftRightCode();
		}
    	Long id = jsonObj.getLong("id");
        EventTypeVo eventType = eventTypeMapper.getEventTypeById(id);
        if(eventType == null) {
        	throw new EventTypeNotFoundException(id);
        }
        Long parentId = jsonObj.getLong("parentId");
        EventTypeVo parentEventType = new EventTypeVo();
		if(EventTypeVo.ROOT_ID.equals(parentId)){
            parentEventType.setId(EventTypeVo.ROOT_ID);
            parentEventType.setName("root");
            parentEventType.setParentId(EventTypeVo.ROOT_PARENTID);
            parentEventType.setLft(1);
            parentEventType.setLayer(0);
        }else{
            parentEventType = eventTypeMapper.getEventTypeById(parentId);
            if(parentEventType == null) {
            	throw new EventTypeNotFoundException(parentId);
            }
        }
        if(Objects.equal(id, parentId)) {
        	throw new EventTypeMoveException("移动后的父节点不可以是当前节点");
        }

        if(!parentId.equals(eventType.getParentId())) {
        	//判断移动后的父节点是否在当前节点的后代节点中
            if(eventTypeMapper.checkEventTypeIsExistsByLeftRightCode(parentId, eventType.getLft(), eventType.getRht()) > 0) {
            	throw new EventTypeMoveException("移动后的父节点不可以是当前节点的后代节点");
            }

            eventType.setParentId(parentId);
            eventTypeMapper.updateEventTypeParentIdById(eventType);
        }

        //将被移动块中的所有节点的左右编码值设置为<=0
        eventTypeMapper.batchUpdateEventTypeLeftRightCodeByLeftRightCode(eventType.getLft(), eventType.getRht(), -eventType.getRht());
 		//计算被移动块右边的节点移动步长
 		int step = eventType.getRht() - eventType.getLft() + 1;
 		//更新旧位置右边的左右编码值
        eventTypeMapper.batchUpdateEventTypeLeftCode(eventType.getLft(), -step);
        eventTypeMapper.batchUpdateEventTypeRightCode(eventType.getLft(), -step);
		
        //找出被移动块移动后左编码值     	
		int lft = 0;
 		int sort = jsonObj.getIntValue("sort");
		if(sort == 0) {//移动到首位
			lft = parentEventType.getLft() + 1;
 		}else {
            EventTypeVo preveventType = eventTypeMapper.getEventTypeByParentIdAndStartNum(parentId, sort);
 			lft = preveventType.getRht() + 1;
 		}
		
		//更新新位置右边的左右编码值
        eventTypeMapper.batchUpdateEventTypeLeftCode(lft, step);
        eventTypeMapper.batchUpdateEventTypeRightCode(lft, step);
		
		//更新被移动块中节点的左右编码值
        eventTypeMapper.batchUpdateEventTypeLeftRightCodeByLeftRightCode(eventType.getLft() - eventType.getRht(), eventType.getRht() - eventType.getRht(), lft - eventType.getLft() + eventType.getRht());

        if(eventTypeMapper.checkLeftRightCodeIsWrong() > 0) {
            eventTypeService.rebuildLeftRightCode();
        }

        //找出被移动节点本身及子节点，计算并更新层级
        EventTypeVo self = eventTypeMapper.getEventTypeById(eventType.getId());
        List<EventTypeVo> childrenAndSelf = eventTypeMapper.getChildrenByLeftRightCode(self.getLft(), self.getRht());
        if(CollectionUtils.isNotEmpty(childrenAndSelf)){
            for(EventTypeVo vo :childrenAndSelf){
                int layer = vo.getLayer() - self.getLayer();
                vo.setLayer(layer);
            }
        }
        self.setLayer(parentEventType.getLayer() + 1);
        childrenAndSelf.add(self);
        for(EventTypeVo vo :childrenAndSelf){
            int layer = vo.getLayer() + self.getLayer();
            if(!vo.getId().equals(self.getId())){
                vo.setLayer(layer);
            }
            eventTypeMapper.updateEventTypeLayer(vo);
        }
        /** 查询被移动节点及其子节点关联的解决方案数量，保证移动后页面回显的数据正确 */
        List<EventTypeVo> typeVos = eventTypeMapper.getChildrenByLeftRightCode(self.getLft(), self.getRht());
        typeVos.add(self);
        List<Map<String,Object>> idSolutionCountMapList = new ArrayList<>();
        for(EventTypeVo vo : typeVos){
            EventTypeVo count = eventTypeMapper.getEventTypeSolutionCountByLftRht(vo.getLft(), vo.getRht());
            Map<String,Object> map = new HashMap<>();
            map.put("id",vo.getId());
            map.put("solutionCount",count.getSolutionCount());
            idSolutionCountMapList.add(map);
        }
        JSONObject result = new JSONObject();
        result.put("idSolutionCountMapList",idSolutionCountMapList);
        return result;
    }
}
