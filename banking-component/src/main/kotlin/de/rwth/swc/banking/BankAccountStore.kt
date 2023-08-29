package de.rwth.swc.banking

import org.springframework.stereotype.Component

interface BankAccountStore {
    fun addBankAccount(iban: String, balance: Int)
    fun getBalance(iban: String): Int
    fun setBalance(iban: String, balance: Int)
    fun clear()
}

@Component
class BankAccountStoreImpl : BankAccountStore {
    private val bankAccounts = mutableMapOf<String, Int>()

    override fun addBankAccount(iban: String, balance: Int) {
        bankAccounts[iban] = balance
    }

    override fun getBalance(iban: String): Int {
        return bankAccounts[iban] ?: throw IllegalArgumentException("No bank account with IBAN $iban")
    }

    override fun setBalance(iban: String, balance: Int) {
        if (bankAccounts[iban] == null) throw IllegalArgumentException("No bank account with IBAN $iban")
        bankAccounts[iban] = balance
    }

    override fun clear() {
        bankAccounts.clear()
    }
}