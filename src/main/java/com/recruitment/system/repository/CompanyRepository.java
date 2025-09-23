package com.recruitment.system.repository;

import com.recruitment.system.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho Company entity
 */
@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    Optional<Company> findByBusinessLicense(String businessLicense);

    Optional<Company> findByTaxCode(String taxCode);

    List<Company> findByIsVerified(Boolean isVerified);

    List<Company> findByIndustry(String industry);

    List<Company> findByCity(String city);

    Page<Company> findByIsVerified(Boolean isVerified, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.industry) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Company> searchCompanies(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Company c WHERE c.city = :city AND c.isVerified = true")
    List<Company> findVerifiedCompaniesByCity(@Param("city") String city);

    @Query("SELECT c FROM Company c WHERE c.industry = :industry AND c.isVerified = true")
    List<Company> findVerifiedCompaniesByIndustry(@Param("industry") String industry);

    @Query("SELECT COUNT(c) FROM Company c WHERE c.isVerified = true")
    Long countVerifiedCompanies();

    @Query("SELECT c FROM Company c JOIN c.jobPostings jp GROUP BY c.id ORDER BY COUNT(jp) DESC")
    List<Company> findCompaniesOrderByJobPostingsCount(Pageable pageable);

    boolean existsByName(String name);

    boolean existsByBusinessLicense(String businessLicense);

    boolean existsByTaxCode(String taxCode);
}