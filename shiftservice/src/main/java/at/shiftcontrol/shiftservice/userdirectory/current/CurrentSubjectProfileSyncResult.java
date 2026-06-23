package at.shiftcontrol.shiftservice.userdirectory.current;

import at.shiftcontrol.lib.entity.UserAccount;

public record CurrentSubjectProfileSyncResult(
    CurrentSubjectProfile currentSubjectProfile,
    UserAccount userAccount
) {}
