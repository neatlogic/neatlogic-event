package codedriver.module.event.stephandler.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Objects;

import codedriver.framework.process.constvalue.ProcessStepHandlerType;
import codedriver.framework.process.constvalue.ProcessStepMode;
import codedriver.framework.process.constvalue.ProcessTaskAuditDetailType;
import codedriver.framework.process.constvalue.ProcessTaskAuditType;
import codedriver.framework.process.dto.ProcessTaskStepVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.module.event.constvalue.EventAuditDetailType;
import codedriver.module.event.dao.mapper.EventMapper;
import codedriver.module.event.dto.EventVo;
import codedriver.module.event.dto.ProcessTaskStepEventVo;
import codedriver.module.event.exception.core.EventNotFoundException;
@Service
public class EventProcessComponent extends ProcessStepHandlerBase {
    
    @Autowired
    private EventMapper eventMapper;
    
    @Override
    public String getHandler() {
        return ProcessStepHandlerType.EVENT.getHandler();
    }
    
    @SuppressWarnings("serial")
    @Override
    public JSONObject getChartConfig() {
        return new JSONObject() {
            {
                this.put("icon", "tsfont-checklist");
                this.put("shape", "L-rectangle:R-rectangle");
                this.put("width", 68);
                this.put("height", 40);
            }
        };
    }

    @Override
    public String getType() {
        return ProcessStepHandlerType.EVENT.getType();
    }

    @Override
    public ProcessStepMode getMode() {
        return ProcessStepMode.MT;
    }

    @Override
    public String getName() {
        return ProcessStepHandlerType.EVENT.getName();
    }

    @Override
    public int getSort() {
        return 4;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public Boolean isAllowStart() {
        return false;
    }

    @Override
    protected int myActive(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, Set<ProcessTaskStepWorkerVo> workerSet)
        throws ProcessTaskException {
        return defaultAssign(currentProcessTaskStepVo, workerSet);
    }

    @Override
    protected int myHang(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myHandle(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myStart(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myComplete(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        JSONObject paramObj = currentProcessTaskStepVo.getParamObj();
        JSONObject handlerStepInfoObj = paramObj.getJSONObject("handlerStepInfo");
        EventVo eventVo = JSON.toJavaObject(handlerStepInfoObj, EventVo.class);
        if(eventVo != null) {
            Long eventId = eventMapper.getEventIdByProcessTaskStepId(currentProcessTaskStepVo.getId());
            if(eventId != null) {
                EventVo oldEventVo = eventMapper.getEventById(eventId);
                if(oldEventVo == null) {
                    throw new EventNotFoundException(eventId);
                }
                if(!Objects.equal(oldEventVo.getEventTypeId(), eventVo.getEventTypeId()) || !Objects.equal(oldEventVo.getEventSolutionId(), eventVo.getEventSolutionId())) {
                    eventVo.setId(oldEventVo.getId());
                    eventMapper.updateEvent(eventVo);
                }
            }else {
                eventMapper.insertEvent(eventVo);
                eventMapper.insetProcessTaskStepEvent(new ProcessTaskStepEventVo(currentProcessTaskStepVo.getProcessTaskId(), currentProcessTaskStepVo.getId(), eventVo.getId()));
            }
        }
        return 1;
    }

    @Override
    protected int myCompleteAudit(ProcessTaskStepVo currentProcessTaskStepVo) {
        if(StringUtils.isNotBlank(currentProcessTaskStepVo.getError())) {
            currentProcessTaskStepVo.getParamObj().put(ProcessTaskAuditDetailType.CAUSE.getParamName(), currentProcessTaskStepVo.getError());
        }
        /** 处理历史记录 **/
        String action = currentProcessTaskStepVo.getParamObj().getString("action");
        JSONObject handlerStepInfoObj = currentProcessTaskStepVo.getParamObj().getJSONObject("handlerStepInfo");
        if (MapUtils.isNotEmpty(handlerStepInfoObj)) {
            currentProcessTaskStepVo.getParamObj().put(EventAuditDetailType.EVENTINFO.getParamName(), JSON.toJSONString(handlerStepInfoObj));
        }
        AuditHandler.audit(currentProcessTaskStepVo, ProcessTaskAuditType.getProcessTaskAuditType(action));
        return 1;
    }

    @Override
    protected int myRetreat(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myAbort(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myRecover(ProcessTaskStepVo currentProcessTaskStepVo) {
        return 0;
    }

    @Override
    protected int myTransfer(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList)
        throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myBack(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int mySaveDraft(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected int myStartProcess(ProcessTaskStepVo processTaskStepVo) throws ProcessTaskException {
        return 0;
    }

    @Override
    protected Set<ProcessTaskStepVo> myGetNext(ProcessTaskStepVo currentProcessTaskStepVo,
        List<ProcessTaskStepVo> nextStepList, Long nextStepId) throws ProcessTaskException {
        Set<ProcessTaskStepVo> nextStepSet = new HashSet<>();
        if (nextStepList.size() == 1) {
            nextStepSet.add(nextStepList.get(0));
        } else if (nextStepList.size() > 1) {
            if(nextStepId == null) {
                throw new ProcessTaskException("找到多个后续节点");
            }
            for (ProcessTaskStepVo processTaskStepVo : nextStepList) {
                if (processTaskStepVo.getId().equals(nextStepId)) {
                    nextStepSet.add(processTaskStepVo);
                    break;
                }
            }
        }
        return nextStepSet;
    }

    @Override
    protected int myPause(ProcessTaskStepVo currentProcessTaskStepVo) throws ProcessTaskException {
        // TODO Auto-generated method stub
        return 0;
    }

}
