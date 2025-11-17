package br.com.safe_line.safeline.modules.report.controller;

import br.com.safe_line.safeline.modules.report.dto.ReportRequestDTO;
import br.com.safe_line.safeline.modules.report.service.ReportService;
import br.com.safe_line.safeline.modules.response.BaseResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/report")
@AllArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<BaseResponse<ReportRequestDTO>> createReportController(@RequestBody ReportRequestDTO reportRequestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createdReport(reportRequestDTO));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<Set<ReportRequestDTO>>> getReportController() {
        return ResponseEntity.status(HttpStatus.OK).body(reportService.getAllReport());
    }

}