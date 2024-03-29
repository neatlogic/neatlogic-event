package neatlogic.module.event.dao.mapper;

import neatlogic.framework.event.dto.EventTypeVo;
import org.apache.ibatis.annotations.Param;

import neatlogic.framework.event.dto.EventSolutionVo;

import java.util.List;

public interface EventSolutionMapper {

	public List<EventSolutionVo> searchSolution(EventSolutionVo eventSolutionVo);

	public EventSolutionVo getSolutionById(@Param("id") Long id);

	public int searchSolutionCount(EventSolutionVo eventSolutionVo);

	public EventSolutionVo checkSolutionExistsById(@Param("id") Long id);

	public int checkSolutionNameIsRepeat(EventSolutionVo vo);

	public List<EventTypeVo> getEventTypeBySolutionId(@Param("solutionId") Long solutionId);

	public int updateSolutionById(EventSolutionVo eventSolutionVo);

	public void updateSolutionStatus(EventSolutionVo eventSolutionVo);

	public int insertSolution(EventSolutionVo eventSolutionVo);

	public int insertEventTypeSolution(@Param("eventTypeId") Long eventTypeId,@Param("solutionId") Long solutionId);

	public int deleteEventTypeSolution(@Param("solutionId") Long solutionId);

	public int deleteSolution(@Param("id") Long solutionId);
}
