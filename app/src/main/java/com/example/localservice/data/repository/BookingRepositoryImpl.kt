package com.example.localservice.data.repository

import com.example.localservice.data.local.dao.BookingDao
import com.example.localservice.data.local.entity.BookingEntity
import com.example.localservice.data.remote.firebase.BookingFirestoreSource
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.domain.model.Stage
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val source: BookingFirestoreSource,
    private val bookingDao: BookingDao
) : BookingRepository {

    override suspend fun createBooking(booking: Booking): Result<Booking> {
        val result = source.createBooking(booking)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { bookingDao.upsert(result.data.toEntity()) }
        }
        return result
    }

    override fun getBookingsForClient(clientUid: String): Flow<Result<List<Booking>>> = flow {
        val roomFlow = bookingDao.getBookingsForClient(clientUid).map { entities ->
            Result.Success(entities.map { it.toDomain() }) as Result<List<Booking>>
        }
        coroutineScope {
            launch {
                source.getBookingsForClient(clientUid).collect { result ->
                    if (result is Result.Success) {
                        withContext(Dispatchers.IO) {
                            bookingDao.upsertAll(result.data.map { it.toEntity() })
                        }
                    }
                }
            }
            emitAll(roomFlow)
        }
    }

    override fun getBookingsForProvider(providerUid: String): Flow<Result<List<Booking>>> = flow {
        val roomFlow = bookingDao.getBookingsForProvider(providerUid).map { entities ->
            Result.Success(entities.map { it.toDomain() }) as Result<List<Booking>>
        }
        coroutineScope {
            launch {
                source.getBookingsForProvider(providerUid).collect { result ->
                    if (result is Result.Success) {
                        withContext(Dispatchers.IO) {
                            bookingDao.upsertAll(result.data.map { it.toEntity() })
                        }
                    }
                }
            }
            emitAll(roomFlow)
        }
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> {
        val result = source.updateBookingStatus(bookingId, status)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) {
                bookingDao.getBookingById(bookingId)?.let { entity ->
                    bookingDao.upsert(entity.copy(status = status.name))
                }
            }
        }
        return result
    }

    override suspend fun updateBookingBudget(bookingId: String, amount: Int, note: String): Result<Unit> {
        val result = source.updateBookingBudget(bookingId, amount, note)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) {
                bookingDao.getBookingById(bookingId)?.let { entity ->
                    bookingDao.upsert(
                        entity.copy(
                            status = BookingStatus.BUDGET_SENT.name,
                            budgetAmount = amount,
                            budgetNote = note
                        )
                    )
                }
            }
        }
        return result
    }

    override suspend fun updateStages(bookingId: String, stages: List<Stage>): Result<Unit> {
        val result = source.updateStages(bookingId, stages)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) {
                bookingDao.getBookingById(bookingId)?.let { entity ->
                    bookingDao.upsert(entity.copy(stagesJson = com.example.localservice.data.local.Converters().let {
                        it.fromStageList(stages)
                    }))
                }
            }
        }
        return result
    }

    override fun getBookingBySlug(slug: String): Flow<Result<Booking>> = flow {
        val roomFlow = bookingDao.getBookingBySlug(slug).map { entity ->
            if (entity != null) Result.Success(entity.toDomain())
            else Result.Error("Trabajo no encontrado")
        }
        coroutineScope {
            launch {
                source.getBookingBySlug(slug).collect { result ->
                    if (result is Result.Success) {
                        withContext(Dispatchers.IO) { bookingDao.upsert(result.data.toEntity()) }
                    }
                }
            }
            emitAll(roomFlow)
        }
    }

    override suspend fun getBookingById(bookingId: String): Result<Booking> {
        val remote = source.getBookingById(bookingId)
        if (remote is Result.Success) {
            withContext(Dispatchers.IO) { bookingDao.upsert(remote.data.toEntity()) }
            return remote
        }
        val cached = withContext(Dispatchers.IO) { bookingDao.getBookingById(bookingId) }
        return if (cached != null) Result.Success(cached.toDomain())
        else remote
    }

    private fun Booking.toEntity() = BookingEntity(
        id = id,
        providerUid = providerUid,
        providerName = providerName,
        clientUid = clientUid,
        clientName = clientName,
        clientPhone = clientPhone,
        category = category.name,
        description = description,
        status = status.name,
        budgetAmount = budgetAmount,
        budgetApproved = budgetApproved,
        budgetNote = budgetNote,
        publicSlug = publicSlug,
        stagesJson = com.example.localservice.data.local.Converters().fromStageList(stages),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun BookingEntity.toDomain() = Booking(
        id = id,
        providerUid = providerUid,
        providerName = providerName,
        clientUid = clientUid,
        clientName = clientName,
        clientPhone = clientPhone,
        category = ServiceCategory.valueOf(category),
        description = description,
        status = BookingStatus.valueOf(status),
        budgetAmount = budgetAmount,
        budgetApproved = budgetApproved,
        budgetNote = budgetNote,
        publicSlug = publicSlug,
        stages = com.example.localservice.data.local.Converters().toStageList(stagesJson),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
