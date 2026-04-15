package az.abb.payment.repository;


import az.abb.payment.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCif(Long cif);
    Optional<Account> findByUserId(Long userId);

//    @Query(value = "Select cif_sequence.NEXTVAL FROM dual",  nativeQuery = true) -- for oracle
    @Query(value = "Select nextval('cif_sequence')",  nativeQuery = true)
    Long getNextCifValue();
}
