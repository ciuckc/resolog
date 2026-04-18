package com.resolog.catalog.domain.model;

import com.resolog.catalog.config.JpaConfig;
import com.resolog.catalog.domain.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaConfig.class)
public class TrackTest {

    @Autowired
    private TrackRepository trackRepository;

}
