package ru.yjailbir.accountsservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yjailbir.accountsservice.entity.AccountEntity;

@Repository
public interface AccountsRepository extends JpaRepository<AccountEntity, Long> {
    AccountEntity findByCurrencyAndUser_Id(String currency, Long userId);
}
