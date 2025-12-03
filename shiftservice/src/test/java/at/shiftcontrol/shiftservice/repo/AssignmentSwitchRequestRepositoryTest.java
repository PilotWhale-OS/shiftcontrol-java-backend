package at.shiftcontrol.shiftservice.repo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import config.TestConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequest;

@DataJpaTest
@Import({TestConfig.class})
public class AssignmentSwitchRequestRepositoryTest {
    @Autowired
    private AssignmentSwitchRequestRepository assignmentSwitchRequestRepository;

    @Test
    void testGetAllAssignmentSwitchRequests() {
        List<AssignmentSwitchRequest> assignmentSwitchRequests = assignmentSwitchRequestRepository.findAll();
        Assertions.assertFalse(assignmentSwitchRequests.isEmpty());
    }
}
