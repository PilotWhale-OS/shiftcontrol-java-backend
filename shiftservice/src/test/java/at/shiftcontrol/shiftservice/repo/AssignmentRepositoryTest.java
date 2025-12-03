package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.Assignment;

@DataJpaTest
@Import({TestConfig.class})
public class AssignmentRepositoryTest {
    @Autowired
    private AssignmentRepository assignmentRepository;

    @Test
    void testGetAllAssignments() {
        List<Assignment> assignments = assignmentRepository.findAll();
        Assertions.assertFalse(assignments.isEmpty());
    }
}
