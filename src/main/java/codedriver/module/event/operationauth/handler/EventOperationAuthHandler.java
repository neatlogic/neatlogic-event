package codedriver.module.event.operationauth.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import codedriver.framework.asynchronization.threadlocal.UserContext;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dao.mapper.ProcessTaskMapper;
import codedriver.framework.process.dto.ProcessTaskStepUserVo;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskVo;
import codedriver.framework.process.operationauth.core.IOperationAuthHandler;
import codedriver.framework.process.operationauth.core.IOperationAuthHandlerType;
@Service
public class EventOperationAuthHandler implements IOperationAuthHandler {

    private final Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> operationBiPredicateMap = new HashMap<>();
    @Autowired
    private ProcessTaskMapper processTaskMapper;
    
    @PostConstruct
    public void init() {
        
        operationBiPredicateMap.put(ProcessTaskOperationType.CREATESUBTASK, (processTaskVo, processTaskStepVo) -> {
            if (processTaskStepVo.getIsActive() == 1) {
                if (ProcessTaskStatus.RUNNING.getValue().equals(processTaskStepVo.getStatus()) || ProcessTaskStatus.DRAFT.getValue().equals(processTaskStepVo.getStatus())) {
                    if(processTaskMapper.checkIsProcessTaskStepUser(new ProcessTaskStepUserVo(processTaskStepVo.getProcessTaskId(), processTaskStepVo.getId(), UserContext.get().getUserUuid(), ProcessUserType.MAJOR.getValue())) > 0) {
                        return true;
                    }
                }
            }
            return false;
        });
        
    }
    
    @Override
    public Map<ProcessTaskOperationType, BiPredicate<ProcessTaskVo, ProcessTaskStepVo>> getOperationBiPredicateMap() {
        return operationBiPredicateMap;
    }

    @Override
    public IOperationAuthHandlerType getHandler() {
        return EventOperationAuthHandlerType.EVENT;
    }

}
