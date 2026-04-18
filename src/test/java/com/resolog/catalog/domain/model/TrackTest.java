package com.resolog.catalog.domain.model;

import com.resolog.catalog.domain.repository.TrackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
public class TrackTest {

    @Autowired
    private TrackRepository trackRepository;

}
