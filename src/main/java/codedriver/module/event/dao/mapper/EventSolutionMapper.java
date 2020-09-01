package codedriver.module.event.dao.mapper;

import codedriver.module.event.dto.EventTypeVo;
import org.apache.ibatis.annotations.Param;

import codedriver.module.event.dto.EventSolutionVo;

import java.util.List;

public interface EventSolutionMapper {

	public List<EventSolutionVo> searchSolution(EventSolutionVo eventSolutionVo);

	public EventSolutionVo getSolutionById(@Param("id") Long id);

	public int searchSolutionCount(EventSolutionVo eventSolutionVo);

	public EventSolutionVo checkSolutionExistsById(@Param("id") Long id);

	public EventSolutionVo checkSolutionExistsByName(@Param("name") String name);

	public List<EventTypeVo> getEventTypeBySolutionId(@Param("solutionId") Long solutionId);

	public int updateSolutionById(EventSolutionVo eventSolutionVo);

	public int insertSolution(EventSolutionVo eventSolutionVo);

	public int insertEventTypeSolution(@Param("eventTypeId") Long eventTypeId,@Param("solutionId") Long solutionId);

	public int deleteEventTypeSolution(@Param("solutionId") Long solutionId);

	public int deleteSolution(@Param("id") Long solutionId);
}
