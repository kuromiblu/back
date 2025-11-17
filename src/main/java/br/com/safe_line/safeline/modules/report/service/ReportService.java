package br.com.safe_line.safeline.modules.report.service;

import br.com.safe_line.safeline.modules.report.dto.ReportRequestDTO;
import br.com.safe_line.safeline.modules.report.dto.ReportResponseDTO;
import br.com.safe_line.safeline.modules.report.exception.EmailAlreadyExistsException;
import br.com.safe_line.safeline.modules.report.model.Report;
import br.com.safe_line.safeline.modules.report.repository.ReportRepository;
import br.com.safe_line.safeline.modules.response.BaseResponse;
import br.com.safe_line.safeline.modules.user.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    // método para criar reports
    public BaseResponse<ReportResponseDTO> createdReport(ReportRequestDTO reportRequestDTO) {

        var user = userRepository.findById(reportRequestDTO.userId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        var reportSaved = reportRepository.save(
                Report.builder()
                        .user(user)
                        .phone(reportRequestDTO.phone())
                        .callDate(reportRequestDTO.callDate())
                        .company(reportRequestDTO.company())
                        .description(reportRequestDTO.description())
                        .status(true)
                        .build()
        );

        return BaseResponse.success(
                "Denúncia feita com sucesso!",
                ReportResponseDTO.builder()
                        .id(reportSaved.getId())
                        .phone(reportSaved.getPhone())
                        .callDate(reportSaved.getCallDate())
                        .company(reportSaved.getCompany())
                        .description(reportSaved.getDescription())
                        .status(reportSaved.getStatus())
                        .build(),
                HttpStatus.CREATED.value()
        );
    }

    // método para retornar reports
    public BaseResponse<Set<ReportResponseDTO>> getAllReport() {

        var reports = reportRepository.findAll().stream()
                .map(report -> ReportResponseDTO.builder()
                        .id(report.getId())
                        .phone(report.getPhone())
                        .callDate(report.getCallDate())
                        .company(report.getCompany())
                        .description(report.getDescription())
                        .status(report.getStatus())
                        .build()
                ).collect(Collectors.toSet());

        return BaseResponse.success(
                "Denúncias encontradas com sucesso!",
                reports,
                HttpStatus.OK.value()
        );
    }
}