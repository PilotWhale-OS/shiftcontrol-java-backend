package at.shiftcontrol.shiftservice.endpoint.user;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping(value = "api/v1/events/{eventId}/shift-plans/{shiftPlanId}", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class UserAdministrationEndpoint {

}
