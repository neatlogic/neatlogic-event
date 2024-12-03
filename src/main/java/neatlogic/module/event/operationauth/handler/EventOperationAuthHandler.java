package neatlogic.module.event.operationauth.handler;

import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.event.constvalue.EventProcessStepHandlerType;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.ProcessTaskVo;
import neatlogic.framework.process.exception.operationauth.ProcessTaskPermissionDeniedException;
import neatlogic.framework.process.operationauth.core.OperationAuthHandlerBase;
import neatlogic.framework.process.operationauth.core.TernaryPredicate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventOperationAuthHandler extends OperationAuthHandlerBase {

    private final Map<IOperationType,
        TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<IOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>> operationBiPredicateMap = new HashMap<>();

    @PostConstruct
    public void init() {

    }

    @Override
    public Map<IOperationType, TernaryPredicate<ProcessTaskVo, ProcessTaskStepVo, String, Map<Long, Map<IOperationType, ProcessTaskPermissionDeniedException>>, JSONObject>>
        getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

    @Override
    public String getHandler() {
        return EventProcessStepHandlerType.EVENT.getHandler();
    }

}
