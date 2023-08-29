package de.rwth.swc.banking

data class Transfer(val fromIban: String, val toIban: String, val amount: Int)