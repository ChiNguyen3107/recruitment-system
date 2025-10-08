package com.recruitment.system.entity;

import com.recruitment.system.enums.ProfileDocumentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProfileDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private ProfileDocumentType type;

    @Column(name = "file_name", nullable = false, length = 200)
    private String fileName;

    @Column(name = "file_extension", length = 50)
    private String fileExtension;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "path", nullable = false, length = 500)
    private String path;

    @CreatedDate
    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;
}


