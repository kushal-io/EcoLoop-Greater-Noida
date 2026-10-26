package com.ecoloop.greaternoida.data.model

import com.ecoloop.greaternoida.domain.model.Partner
import com.google.firebase.firestore.PropertyName

data class PartnerDto(
    var name: String = "",
    var address: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    @get:PropertyName("waste_types_accepted")
    @set:PropertyName("waste_types_accepted")
    var wasteTypesAccepted: List<String> = emptyList(),
    var verified: Boolean = false,
    @get:PropertyName("qr_code_id")
    @set:PropertyName("qr_code_id")
    var qrCodeId: String = ""
) {
    fun toDomain(documentId: String): Partner {
        return Partner(
            id = documentId,
            name = name,
            address = address,
            lat = lat,
            lng = lng,
            wasteTypesAccepted = wasteTypesAccepted,
            verified = verified,
            qrCodeId = qrCodeId
        )
    }
}
