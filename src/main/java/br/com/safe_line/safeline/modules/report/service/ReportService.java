package br.com.safe_line.safeline.modules.report.service;

import br.com.safe_line.safeline.modules.report.dto.ReportRequestDTO;
import br.com.safe_line.safeline.modules.report.exception.EmailAlreadyExistsException;
import br.com.safe_line.safeline.modules.report.model.Report;
import br.com.safe_line.safeline.modules.report.repository.ReportRepository;
import br.com.safe_line.safeline.modules.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    //metodo para criar reports
    public BaseResponse<ReportRequestDTO> createdReport(ReportRequestDTO reportRequestDTO) {

        var reportSaved = reportRepository.save(Report.builder().phone(reportRequestDTO.phone()).callDate(reportRequestDTO.callDate())
                .company(reportRequestDTO.company()).description(reportRequestDTO.description()).build());

        return BaseResponse.success("denuncia feita com sucesso!", ReportRequestDTO.builder()
                .phone(reportSaved.getPhone()).callDate(reportSaved.getCallDate()).company(reportSaved.getCompany())
                        .description(reportSaved.getDescription()).build(), HttpStatus.CREATED.value());

    }

    //metodo para retornar reports
    public BaseResponse<Set<ReportRequestDTO>> getAllReport(){
        var reports = reportRepository.findAll().stream().map(report ->  ReportRequestDTO.builder()
                .phone(report.getPhone())
                .callDate(report.getCallDate())
                .company(report.getCompany())
                .description(report.getDescription()).build()).collect(Collectors.toSet());
        return BaseResponse.success("denuncias encontrados com sucesso", reports, HttpStatus.OK.value());
    }

}