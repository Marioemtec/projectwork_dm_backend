package com.clinicaplus.service;

import com.clinicaplus.dto.MedicalReportDTO;
import com.clinicaplus.exception.ResourceNotFoundException;
import com.clinicaplus.model.Appointment;
import com.clinicaplus.model.Doctor;
import com.clinicaplus.model.MedicalReport;
import com.clinicaplus.model.Patient;
import com.clinicaplus.repository.AppointmentRepository;
import com.clinicaplus.repository.DoctorRepository;
import com.clinicaplus.repository.MedicalReportRepository;
import com.clinicaplus.repository.PatientRepository;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MedicalReportService {

    private final MedicalReportRepository medicalReportRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public MedicalReportDTO createReport(Long appointmentId, Long doctorIdOrUserId, String title, String diagnosis, String prescription, String notes) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Try to find doctor by userId first (doctor loading own reports)
        // Then try by doctor.id
        Doctor doctor = doctorRepository.findByUserId(doctorIdOrUserId)
                .orElseGet(() -> doctorRepository.findById(doctorIdOrUserId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Doctor not found with id or userId: " + doctorIdOrUserId)));

        MedicalReport report = MedicalReport.builder()
                .appointment(appointment)
                .doctor(doctor)
                .title(title)
                .diagnosis(diagnosis)
                .prescription(prescription)
                .notes(notes)
                .build();

        MedicalReport savedReport = medicalReportRepository.save(report);
        return mapToDTO(savedReport);
    }

    public MedicalReportDTO getReportById(Long reportId) {
        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));
        return mapToDTO(report);
    }

    public List<MedicalReportDTO> getReportsByAppointmentId(Long appointmentId) {
        return medicalReportRepository.findByAppointmentId(appointmentId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MedicalReportDTO> getReportsByPatientId(Long patientIdOrUserId) {
        // Try to find patient by userId first (when frontend passes user.id)
        // Then try by patient.id (when passing actual patient entity id)
        Patient patient = patientRepository.findByUserId(patientIdOrUserId)
                .orElseGet(() -> patientRepository.findById(patientIdOrUserId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Patient not found with id or userId: " + patientIdOrUserId)));
        
        return medicalReportRepository.findByAppointmentPatientId(patient.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<MedicalReportDTO> getReportsByDoctorId(Long doctorIdOrUserId) {
        // Try to find doctor by userId first (when frontend passes user.id)
        // Then try by doctor.id (when passing actual doctor entity id)
        Doctor doctor = doctorRepository.findByUserId(doctorIdOrUserId)
                .orElseGet(() -> doctorRepository.findById(doctorIdOrUserId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Doctor not found with id or userId: " + doctorIdOrUserId)));
        
        return medicalReportRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public MedicalReportDTO updateReport(Long reportId, String title, String diagnosis, String prescription, String notes) {
        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        report.setTitle(title);
        report.setDiagnosis(diagnosis);
        report.setPrescription(prescription);
        report.setNotes(notes);

        MedicalReport updatedReport = medicalReportRepository.save(report);
        return mapToDTO(updatedReport);
    }

    public void deleteReport(Long reportId) {
        if (!medicalReportRepository.existsById(reportId)) {
            throw new ResourceNotFoundException("Report not found with id: " + reportId);
        }
        medicalReportRepository.deleteById(reportId);
    }

    public byte[] generateReportPDF(Long reportId) {
        MedicalReport report = medicalReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + reportId));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        Document document = new Document(pdfDoc);

        // Title
        document.add(new Paragraph("REFERTO MEDICO")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));

        document.add(new Paragraph(""));

        // Report details
        document.add(new Paragraph("Titolo Referto:")
                .setBold());
        document.add(new Paragraph(report.getTitle()));

        // Doctor information (prominente)
        document.add(new Paragraph("\nRedatto da:")
                .setBold()
                .setFontSize(12));
        document.add(new Paragraph("Dr. " + report.getDoctor().getUser().getFirstName() + " " + 
                report.getDoctor().getUser().getLastName())
                .setFontSize(11)
                .setBold());
        document.add(new Paragraph("Specializzazione: " + report.getDoctor().getSpecialization())
                .setFontSize(10));

        // Date
        document.add(new Paragraph("\nData Referto:")
                .setBold());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        document.add(new Paragraph(report.getCreatedAt().format(formatter)));

        // Patient information
        document.add(new Paragraph("\nPaziente:")
                .setBold());
        document.add(new Paragraph(report.getAppointment().getPatient().getUser().getFirstName() + " " +
                report.getAppointment().getPatient().getUser().getLastName()));

        // Diagnosis
        document.add(new Paragraph("\nDiagnosi:")
                .setBold()
                .setFontSize(12));
        document.add(new Paragraph(report.getDiagnosis()));

        // Prescription
        if (report.getPrescription() != null && !report.getPrescription().isEmpty()) {
            document.add(new Paragraph("\nPrescrizione:")
                    .setBold()
                    .setFontSize(12));
            document.add(new Paragraph(report.getPrescription()));
        }

        // Notes
        if (report.getNotes() != null && !report.getNotes().isEmpty()) {
            document.add(new Paragraph("\nNote Aggiuntive:")
                    .setBold());
            document.add(new Paragraph(report.getNotes()));
        }

        // Footer with doctor signature
        document.add(new Paragraph("\n\n\n"));
        document.add(new Paragraph("___________________________")
                .setTextAlignment(TextAlignment.RIGHT));
        document.add(new Paragraph("Dr. " + report.getDoctor().getUser().getFirstName() + " " + 
                report.getDoctor().getUser().getLastName())
                .setTextAlignment(TextAlignment.RIGHT)
                .setBold());
        document.add(new Paragraph(report.getDoctor().getSpecialization())
                .setTextAlignment(TextAlignment.RIGHT)
                .setFontSize(10));

        document.close();
        return outputStream.toByteArray();
    }

    private MedicalReportDTO mapToDTO(MedicalReport report) {
        return MedicalReportDTO.builder()
                .id(report.getId())
                .appointmentId(report.getAppointment().getId())
                .doctorName(report.getDoctor().getUser().getFirstName() + " " + 
                          report.getDoctor().getUser().getLastName())
                .title(report.getTitle())
                .diagnosis(report.getDiagnosis())
                .prescription(report.getPrescription())
                .notes(report.getNotes())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
