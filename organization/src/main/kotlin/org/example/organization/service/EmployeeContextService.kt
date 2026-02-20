package org.example.organization.service

import jakarta.transaction.Transactional
import org.example.organization.EmployeeContext
import org.example.organization.EmployeeContextNotFoundException
import org.example.organization.EmployeeContextRepository
import org.example.organization.EmployeeNotInOrganizationException
import org.example.organization.EmployeeRepository
import org.example.organization.OrganizationNotActiveException
import org.example.organization.OrganizationNotFoundException
import org.example.organization.OrganizationRepository
import org.example.organization.dto.CurrentOrganizationResponse
import org.example.organization.dto.SetCurrentOrganizationRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

interface EmployeeContextService {
    fun setCurrentOrganization(userId: Long, body: SetCurrentOrganizationRequest)
    fun getCurrentOrganization(userId: Long): CurrentOrganizationResponse
}

@Service
class EmployeeContextServiceImpl(
    private val employeeContextRepository: EmployeeContextRepository,
    private val organizationRepository: OrganizationRepository,
    private val employeeRepository: EmployeeRepository
) : EmployeeContextService {

    @Transactional
    override fun setCurrentOrganization(userId: Long, body: SetCurrentOrganizationRequest) {
        val org = organizationRepository.findByIdAndDeletedFalse(body.organizationId)
            ?: throw OrganizationNotFoundException()

        if (!org.isActive) throw OrganizationNotActiveException()

        // user shu organizationda employee bo'lishi shart
        employeeRepository.findByUserIdAndOrganizationIdAndDeletedFalse(userId, org.id!!)
            ?: throw EmployeeNotInOrganizationException()

        val ctx = employeeContextRepository.findEmployeeContextByUserId(userId)

        if (ctx == null) {
            employeeContextRepository.save(
                EmployeeContext(
                    userId = userId,
                    currentOrganization = org,
                    lastAccessedAt = LocalDateTime.now()
                )
            )
        } else {
            ctx.currentOrganization = org
            ctx.lastAccessedAt = LocalDateTime.now()
            employeeContextRepository.save(ctx)
        }
    }

    override fun getCurrentOrganization(userId: Long): CurrentOrganizationResponse {
        println(userId)
        val emp = employeeRepository.findByUserIdAndDeletedFalse(userId)
            ?: throw EmployeeContextNotFoundException()

        val ctx = employeeContextRepository.findEmployeeContextByUserId(userId)
            ?: throw EmployeeContextNotFoundException()

        return CurrentOrganizationResponse(
            organizationId = ctx.currentOrganization.id!!,
            employeeId = emp.id!!,
            userId = emp.userId
        )
    }
}