package proyect5.realEstate.service;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import proyect5.realEstate.persistence.dtos.InputDTO;
import proyect5.realEstate.persistence.dtos.RentReportDTO;

import java.util.Date;
import java.util.List;

public interface RentReportService {
    List<RentReportDTO> generateAllRentalReport(InputDTO inputDTO);
}
