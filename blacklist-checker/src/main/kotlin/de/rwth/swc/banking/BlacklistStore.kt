package de.rwth.swc.banking

import org.springframework.stereotype.Component

interface BlacklistStore {
    fun addIban(iban: String, reason: String)
    fun clear()
    fun isBlacklisted(iban: String): Boolean
    fun getReason(iban: String): String
}

@Component
class BlacklistStoreImpl : BlacklistStore {
    private val blacklist = mutableMapOf<String, String>()

    override fun addIban(iban: String, reason: String) {
        blacklist[iban] = reason
    }

    override fun clear() {
        blacklist.clear()
    }

    override fun isBlacklisted(iban: String): Boolean {
        return blacklist[iban] != null
    }

    override fun getReason(iban: String): String {
        return blacklist[iban] ?: throw IllegalArgumentException("No bank account with IBAN $iban is Blacklisted")
    }
}