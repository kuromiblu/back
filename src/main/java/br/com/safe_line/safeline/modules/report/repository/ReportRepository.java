package br.com.safe_line.safeline.modules.report.repository;

import br.com.safe_line.safeline.modules.report.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {

    //Optional<Report> findByEmail(String email);

}
