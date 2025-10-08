package com.recruitment.system.repository;

import com.recruitment.system.entity.ProfileDocument;
import com.recruitment.system.enums.ProfileDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileDocumentRepository extends JpaRepository<ProfileDocument, Long> {

    List<ProfileDocument> findByProfileId(Long profileId);

    List<ProfileDocument> findByProfileIdAndType(Long profileId, ProfileDocumentType type);

    Optional<ProfileDocument> findByIdAndProfileId(Long id, Long profileId);
}


