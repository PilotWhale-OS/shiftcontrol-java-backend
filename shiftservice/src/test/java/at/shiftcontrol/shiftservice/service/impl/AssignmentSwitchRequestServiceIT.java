package at.shiftcontrol.shiftservice.service.impl;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import at.shiftcontrol.lib.exception.ConflictException;
import at.shiftcontrol.lib.exception.NotFoundException;
import at.shiftcontrol.shiftservice.dto.TradeCreateDto;
import at.shiftcontrol.shiftservice.dto.TradeDto;
import at.shiftcontrol.shiftservice.dto.userprofile.VolunteerDto;
import at.shiftcontrol.shiftservice.entity.AssignmentId;
import at.shiftcontrol.shiftservice.entity.AssignmentSwitchRequestId;
import at.shiftcontrol.shiftservice.type.TradeStatus;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
public class AssignmentSwitchRequestServiceIT {

    @Autowired
    AssignmentSwitchRequestServiceImpl service;

    @Test
    void testCreateShiftTrade() throws ConflictException, NotFoundException {
        TradeCreateDto createDto = TradeCreateDto.builder()
            .offeredPositionSlotId("1")
            .requestedPositionSlotId("3")
            .requestedVolunteers(List.of(VolunteerDto.builder().id("3").build()))
            .build();

        Collection<TradeDto> dtos = service.createShiftTrade(createDto);

        Assertions.assertNotNull(dtos);
        Assertions.assertFalse(dtos.isEmpty());
        dtos.forEach(trade -> Assertions.assertEquals(TradeStatus.OPEN, trade.getStatus()));
    }

    @Test
    void testAcceptShiftTrade() throws ConflictException, NotFoundException {
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "1"),
            new AssignmentId(2L, "2")
        );

        TradeDto dto = service.acceptShiftTrade(id);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.ACCEPTED, dto.getStatus());
    }

    @Test
    void testDeclineShiftTrade() throws NotFoundException {
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "1"),
            new AssignmentId(2L, "2")
        );

        TradeDto dto = service.declineShiftTrade(id);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.REJECTED, dto.getStatus());
    }

    @Test
    void testCancelShiftTrade() throws NotFoundException {
        AssignmentSwitchRequestId id = new AssignmentSwitchRequestId(
            new AssignmentId(1L, "1"),
            new AssignmentId(2L, "2")
        );

        TradeDto dto = service.cancelShiftTrade(id);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(TradeStatus.CANCELED, dto.getStatus());
    }
}
