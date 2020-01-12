package ru.smartel.strike.repository.event;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import ru.smartel.strike.dto.service.sort.EventSortDTO;
import ru.smartel.strike.entity.Event;

import java.util.List;

@Repository
public interface CustomEventRepository {
    boolean isNotParentForAnyConflicts(long eventId);
    List<Long> findIds(Specification<Event> specification, EventSortDTO sortDTO, Integer page, Integer perPage);
}
