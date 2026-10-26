package com.ecoloop.greaternoida.domain.usecase

import com.ecoloop.greaternoida.core.util.Resource
import com.ecoloop.greaternoida.domain.model.Partner
import com.ecoloop.greaternoida.domain.repository.PartnerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPartnersUseCase @Inject constructor(
    private val repository: PartnerRepository
) {
    operator fun invoke(): Flow<Resource<List<Partner>>> {
        return repository.getPartners()
    }
}
