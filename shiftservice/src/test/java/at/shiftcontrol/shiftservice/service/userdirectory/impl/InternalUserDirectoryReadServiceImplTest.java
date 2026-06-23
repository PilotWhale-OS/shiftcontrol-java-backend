package at.shiftcontrol.shiftservice.service.userdirectory.impl;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import at.shiftcontrol.shiftservice.auth.UserType;
import at.shiftcontrol.shiftservice.dto.user.UserSearchDto;
import at.shiftcontrol.shiftservice.userdirectory.DirectoryUser;
import at.shiftcontrol.shiftservice.userdirectory.UserDirectoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalUserDirectoryReadServiceImplTest {
    @Mock
    private UserDirectoryService userDirectoryService;

    @InjectMocks
    private InternalUserDirectoryReadServiceImpl service;

    @Test
    void searchUsers_filtersAndPaginatesLocalDirectoryUsers() {
        when(userDirectoryService.searchUsers(0, 2, UserSearchDto.builder().name("o").build()))
            .thenReturn(at.shiftcontrol.lib.dto.PaginationDto.<DirectoryUser>builder()
                .page(0)
                .pages(2)
                .total(3)
                .items(List.of(
                    user("1", "alice", "Alice", "Anderson"),
                    user("2", "bobby", "Bob", "Builder")
                ))
                .build());

        var result = service.searchUsers(0, 2, UserSearchDto.builder().name("o").build());

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems())
            .extracting(dto -> dto.getVolunteer().getId(), dto -> dto.getVolunteer().getFirstName())
            .containsExactly(
                org.assertj.core.groups.Tuple.tuple("1", "Alice"),
                org.assertj.core.groups.Tuple.tuple("2", "Bob")
            );
    }

    @Test
    void getContacts_mapsBatchLookupToContactDtos() {
        when(userDirectoryService.getUserByIds(List.of("1"))).thenReturn(List.of(
            user("1", "alice", "Alice", "Anderson")
        ));

        var contacts = service.getContacts(List.of("1"));

        assertThat(contacts)
            .extracting("userId", "firstName", "lastName", "email")
            .containsExactly(org.assertj.core.groups.Tuple.tuple("1", "Alice", "Anderson", "alice@example.com"));
    }

    private static DirectoryUser user(String id, String username, String firstName, String lastName) {
        return new DirectoryUser(id, username, firstName, lastName, username + "@example.com", "https://cdn.example.test/profiles/" + id + ".png", UserType.ASSIGNED);
    }
}
