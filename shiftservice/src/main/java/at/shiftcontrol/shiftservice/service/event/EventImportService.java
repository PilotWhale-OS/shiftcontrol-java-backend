package at.shiftcontrol.shiftservice.service.event;

import at.shiftcontrol.shiftservice.dto.event.EventExportDto;
import at.shiftcontrol.shiftservice.dto.event.EventImportResultDto;
import org.springframework.web.multipart.MultipartFile;

public interface EventImportService {
    EventImportResultDto importEvent(MultipartFile file);

    EventExportDto getEventImportTemplate();
}
