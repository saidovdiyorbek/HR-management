package org.example.organization.service

import org.example.organization.EmployeeContextNotFoundException
import org.example.organization.EmployeeContextRepository
import org.example.organization.EmployeeRepository
import org.example.organization.OrganizationAlreadyExistsException
import org.example.organization.OrganizationMapper
import org.example.organization.OrganizationNotFoundException
import org.example.organization.OrganizationRepository
import org.example.organization.SecurityUtil
import org.example.organization.dto.OrganizationAllResponse
import org.example.organization.dto.OrganizationCreateRequest
import org.example.organization.dto.OrganizationFullResponse
import org.example.organization.dto.OrganizationInfo
import org.example.organization.dto.OrganizationUpdateRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface OrganizationService {
    fun create(body: OrganizationCreateRequest)
    fun getAll(): List<OrganizationAllResponse>
    fun getAllPaginated(pageable: Pageable): Page<OrganizationAllResponse>
    fun getOne(id: Long): OrganizationFullResponse
    fun update(id: Long, body: OrganizationUpdateRequest)
    fun delete(id: Long)
    fun getMyOrganizations(): List<Long>
    fun getOrganizationService(userId: Long): OrganizationInfo
}

@Service
class OrganizationServiceImpl(
    private val repository: OrganizationRepository,
    private val mapper: OrganizationMapper,
    private val securityUtil: SecurityUtil,
    private val employeeRepository: EmployeeRepository,
    private val employeeContextRepository: EmployeeContextRepository
) : OrganizationService {

    @Transactional
    override fun create(body: OrganizationCreateRequest) {
        val currentUserId = securityUtil.getCurrentUserId()
        println(currentUserId)
        repository.findByNameIgnoreCase(body.name.trim())?.let {
            throw OrganizationAlreadyExistsException()
        }

        repository.save(mapper.toEntity(body, currentUserId))
    }

    override fun getAll(): List<OrganizationAllResponse> =
        repository.findAllNotDeleted().map { mapper.toAllResponse(it) }

    override fun getAllPaginated(pageable: Pageable): Page<OrganizationAllResponse> =
        repository.findAllNotDeletedForPageable(pageable).map { mapper.toAllResponse(it) }

    override fun getOne(id: Long): OrganizationFullResponse {
        val org = repository.findByIdAndDeletedFalse(id) ?: throw OrganizationNotFoundException()
        return mapper.toFullResponse(org)
    }

    @Transactional
    override fun update(id: Long, body: OrganizationUpdateRequest) {
        val org = repository.findByIdAndDeletedFalse(id) ?: throw OrganizationNotFoundException()

        body.name?.let { newName ->
            val nn = newName.trim()
            repository.findByNameIgnoreCase(nn)?.let { found ->
                if (found.id != org.id) throw OrganizationAlreadyExistsException()
            }
            org.name = nn
        }

        body.description?.let { org.description = it }
        body.isActive?.let { org.isActive = it }

        repository.save(org)
    }

    @Transactional
    override fun delete(id: Long) {
        repository.findByIdAndDeletedFalse(id) ?: throw OrganizationNotFoundException()
        repository.trash(id)
    }

    override fun getMyOrganizations(): List<Long> {
        val currentUserId = securityUtil.getCurrentUserId()

        return employeeRepository.findAllByUserIdAndDeletedFalse(currentUserId)
            .map { it.organization.id!! }
    }

    override fun getOrganizationService(userId: Long): OrganizationInfo {

        println(userId)
        val ctx = employeeContextRepository.findEmployeeContextWithOrganizationInfo(userId)
            ?: throw EmployeeContextNotFoundException()

        return ctx
    }
}
