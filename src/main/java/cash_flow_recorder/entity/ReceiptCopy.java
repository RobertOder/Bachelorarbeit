package cash_flow_recorder.entity;

import cash_flow_recorder.entity.state.NewReceiptCopyState;
import cash_flow_recorder.entity.state.ReceiptCopyState;
import cash_flow_recorder.entity.state.TranslatedReceiptCopyState;
import cash_flow_recorder.service.ReceiptCopyService;
import cash_flow_recorder.service.ExpenditureService;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import cash_flow_recorder.entity.state.*;
import cash_flow_recorder.service.OCRService;

import java.util.Date;

/**
 * Class to represent a photocopy of a receipt
 * Context-Class for the state pattern
 */
@NoArgsConstructor
@Getter
@Setter
@Entity
public class ReceiptCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Date date;
    private String photoPath;
    @Column(columnDefinition = "TEXT")
    private String translation;
    @ManyToOne
    @JoinColumn(name = "expenditure_id")
    @JsonBackReference("Expenditure-ReceiptCopies")
    private Expenditure expenditure;

    @Enumerated(EnumType.STRING)
    private ReceiptCopyStatus status = ReceiptCopyStatus.NEW;

    @Transient
    @JsonIgnore
    private ReceiptCopyState state;

    /**
     * Constructor
     * @param date date the copy was taken
     * @param photoPath the path of the photofile
     * @param translation the digital ocr translation of the photo
     */
    public ReceiptCopy(Date date, String photoPath, String translation) {
        this.date = date;
        this.photoPath = photoPath;
        this.translation = translation;
        this.status = ReceiptCopyStatus.NEW;
        this.setStateByStatus();
    }

    /**
     * Method to check the state and save as status
     */
    @PostLoad
    public void setStateByStatus() {
        switch (this.status) {
            case NEW -> this.state = new NewReceiptCopyState();
            case TRANSLATED -> this.state = new TranslatedReceiptCopyState();
        }
    }

    /**
     * Method of starting the automation process for categorization
     * @param ocrService The implemented OCR-Engine as Service
     */
    public void process(OCRService ocrService, ReceiptCopyService receiptCopyService, ExpenditureService expenditureService) {
        this.state.process(this, ocrService, receiptCopyService, expenditureService);
    }

}
