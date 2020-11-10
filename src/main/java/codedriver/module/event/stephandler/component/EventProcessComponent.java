package codedriver.module.event.stephandler.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import codedriver.framework.process.dto.ProcessTaskStepWorkerPolicyVo;
import codedriver.framework.process.dto.ProcessTaskStepWorkerVo;
import codedriver.framework.process.exception.core.ProcessTaskException;
import codedriver.framework.process.stephandler.core.ProcessStepHandlerBase;
import codedriver.framework.process.workerpolicy.core.IWorkerPolicyHandler;
import codedriver.framework.process.workerpolicy.core.WorkerPolicyHandlerFactory;
import codedriver.module.event.constvalue.EventAuditDetailType;
import codedriver.module.event.dao.mapper.EventMapper;
import codedriver.module.event.dto.EventVo;
import codedriver.module.event.dto.ProcessTaskStepEventVo;
import codedriver.module.event.exception.core.EventNotFoundException;
@Service
public class EventProcessComponent extends ProcessStepHandlerBase {

    private final Logger logger = LoggerFactory.getLogger(EventProcessComponent.class);
    
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
        return ProcessStepHandlerType.EVENT.getHandler();
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
    protected int myAssign(ProcessTaskStepVo currentProcessTaskStepVo, List<ProcessTaskStepWorkerVo> workerList)
        throws ProcessTaskException {
        /** 获取步骤配置信息 **/
        ProcessTaskStepVo processTaskStepVo = processTaskMapper.getProcessTaskStepBaseInfoById(currentProcessTaskStepVo.getId());
        String stepConfig = selectContentByHashMapper.getProcessTaskStepConfigByHash(processTaskStepVo.getConfigHash());

        String executeMode = "";
        int autoStart = 0;
        try {
            JSONObject stepConfigObj = JSONObject.parseObject(stepConfig);
            currentProcessTaskStepVo.getParamObj().putAll(stepConfigObj);
            if (MapUtils.isNotEmpty(stepConfigObj)) {
                JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
                if(MapUtils.isNotEmpty(stepConfigObj)) {
                    executeMode = workerPolicyConfig.getString("executeMode");
                    autoStart = workerPolicyConfig.getIntValue("autoStart");
                }
            }
        } catch (Exception ex) {
            logger.error("hash为" + processTaskStepVo.getConfigHash() + "的processtask_step_config内容不是合法的JSON格式", ex);
        }
        
        /** 如果workerList.size()>0，说明已经存在过处理人，则继续使用旧处理人，否则启用分派 **/
        if (CollectionUtils.isEmpty(workerList))  {
            /** 分配处理人 **/
            ProcessTaskStepWorkerPolicyVo processTaskStepWorkerPolicyVo = new ProcessTaskStepWorkerPolicyVo();
            processTaskStepWorkerPolicyVo.setProcessTaskStepId(currentProcessTaskStepVo.getId());
            List<ProcessTaskStepWorkerPolicyVo> workerPolicyList = processTaskMapper.getProcessTaskStepWorkerPolicy(processTaskStepWorkerPolicyVo);
            if (CollectionUtils.isNotEmpty(workerPolicyList)) {
                for (ProcessTaskStepWorkerPolicyVo workerPolicyVo : workerPolicyList) {
                    IWorkerPolicyHandler workerPolicyHandler = WorkerPolicyHandlerFactory.getHandler(workerPolicyVo.getPolicy());
                    if (workerPolicyHandler != null) {
                        List<ProcessTaskStepWorkerVo> tmpWorkerList = workerPolicyHandler.execute(workerPolicyVo, currentProcessTaskStepVo);
                        /** 顺序分配处理人 **/
                        if ("sort".equals(executeMode) && CollectionUtils.isEmpty(tmpWorkerList)) {
                            // 找到处理人，则退出
                            workerList.addAll(tmpWorkerList);
                            break;
                        } else if ("batch".equals(executeMode)) {
                            // 去重取并集
                            tmpWorkerList.removeAll(workerList);
                            workerList.addAll(tmpWorkerList);
                        }
                    }
                }
            }
        }
        
        return autoStart;
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
                if(!Objects.equal(eventVo.getEventTypeId(), eventVo.getEventTypeId()) || !Objects.equal(eventVo.getEventSolutionId(), eventVo.getEventSolutionId())) {
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
