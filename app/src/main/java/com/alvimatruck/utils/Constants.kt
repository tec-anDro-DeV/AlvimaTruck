package com.alvimatruck.utils

class Constants {
    companion object {

        // Base URL
        const val BASE_URL = "https://api.businesscentral.dynamics.com/v2.0/Sandbox/api/tbs/htseafood/v2.0/companies(988d3bde-ac0d-ee11-8f6e-00224805c459)/" //Sandbox
        //const val BASE_URL = "https://api.businesscentral.dynamics.com/v2.0/Production/api/tbs/htseafood/v2.0/companies(988d3bde-ac0d-ee11-8f6e-00224805c459)/" //live

        const val SIGN_IN = "users"
        const val RECEIVE_ITEM_LIST = "purchOrdHdr"
        const val SHIP_ITEM_LIST = "salesOrdHdr"
        const val PO_DETAILS = "purchOrdLines"
        const val SO_DETAILS = "salesOrdLines"
        const val PO_Temp = "tempPurchaseOrderLine"
        const val SO_Temp = "tempSalesOrderLine"
        const val Location = "location"
        const val ItemTransfer = "itemRecJournalLine"
        const val ItemLedgerEntries = "itemLedgerEntriesN"
        const val SearchBarcode = "searchBarcode"
        const val Bin = "bin"


        // Constants Key
        const val IS_LOGIN = "is_login"
        const val CustmerNo = "custmerNo"
        const val UserName = "userName"
        const val UserEmail = "userEmail"

    }


}