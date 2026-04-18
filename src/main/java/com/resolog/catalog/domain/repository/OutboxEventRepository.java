package com.resolog.catalog.domain.repository;

import com.resolog.catalog.domain.model.OutboxEvent;
import com.resolog.catalog.domain.model.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatus(OutboxEventStatus eventStatus);

}
