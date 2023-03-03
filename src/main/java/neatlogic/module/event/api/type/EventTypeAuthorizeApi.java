package neatlogic.module.event.api.type;

import neatlogic.framework.auth.core.AuthAction;
import neatlogic.framework.common.constvalue.ApiParamType;
import neatlogic.framework.dto.AuthorityVo;
import neatlogic.framework.restful.annotation.Description;
import neatlogic.framework.restful.annotation.Input;
import neatlogic.framework.restful.annotation.OperationType;
import neatlogic.framework.restful.annotation.Param;
import neatlogic.framework.restful.constvalue.OperationTypeEnum;
import neatlogic.framework.restful.core.privateapi.PrivateApiComponentBase;
import neatlogic.framework.event.auth.label.EVENT_TYPE_MODIFY;
import neatlogic.module.event.dao.mapper.EventTypeMapper;
import neatlogic.framework.event.dto.EventTypeVo;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@AuthAction(action = EVENT_TYPE_MODIFY.class)
@Service
@Transactional
@OperationType(type = OperationTypeEnum.OPERATE)
public class EventTypeAuthorizeApi extends PrivateApiComponentBase {

    @Resource
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getToken() {
        return "eventtype/auth/save";
    }

    @Override
    public String getName() {
        return "保存事件类型授权";
    }

    @Override
    public String getConfig() {
        return null;
    }


    @Input({
            @Param(name = "id", type = ApiParamType.LONG, desc = "事件类型ID", isRequired = true),
            @Param(name = "authorityList", type = ApiParamType.JSONARRAY, desc = "授权对象，可多选，格式[\"user#userUuid\",\"team#teamUuid\",\"role#roleUuid\"]")
    })
    @Description(desc = "保存事件类型授权")
    @Override
    public Object myDoService(JSONObject jsonObj) throws Exception {

        Long id = jsonObj.getLong("id");
        EventTypeVo eventType = new EventTypeVo();
        eventType.setId(id);
        //首先删除所有权限
        eventTypeMapper.deleteAuthorityByEventTypeId(id);
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

        return null;
    }
}
